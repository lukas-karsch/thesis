I found the following metrics 
- **Chidamber Kemerer**
	- WMC (Weighted methods per class)
	- CBO (Coupling between objects)
	- LCOM (Lack of cohesion in methods)
	- DIT (Depth of inheritance tree)
	- RFC (Response for a class)
- **Complexity** 
	- **v(G) (Cyclomatic Complexity):** The number of independent paths through a method. In CRUD, your "Services" might have high $v(G)$ due to complex `if/else` logic. In ES, $v(G)$ is often lower because logic is distributed into many small "Command Handlers" or "Projectors."
	- **CogC (Cognitive Complexity):** How hard the code is for a human to read.
	- **iv(G) (Design Complexity):** How much a method’s complexity contributes to the complexity of its callers.
	- **ev(G) (Essential Complexity):** A measure of "structuredness." A value of **1** means the code is perfectly structured; higher values indicate "spaghetti code" logic.
- **Dependencies** 
	- Cyclic dependencies 
	- efferent coupling. number of classes this class depends on directly (dpy) or transitively (dpy*)
	- afferent coupling: number of classes depending on this class directly (dpt) or transitively (dpt*)
	- package dependencies: PDcy, PDpt measure dependencies that cross package boundaries 
- **Martin (Robert C. Martin)**
	- **Ca (Afferent Coupling):** Number of external packages that depend on this package (Incoming).
	- **Ce (Efferent Coupling):** Number of external packages this package depends on (Outgoing).
	- **I (Instability):** Calculated as $I = \frac{Ce}{(Ca + Ce)}$. A value of **0** is maximally stable (hard to change because many depend on it); **1** is maximally unstable (easy to change because no one depends on it).
	- **A (Abstractness):** Ratio of abstract classes/interfaces to total classes. **0** is pure implementation; **1** is pure abstraction.
	- **D (Distance from Main Sequence):** Calculated as $D = |A + I - 1|$. This is your most important "Flexibility" metric.
	    - **$D \approx 0$**: The package is in "The Main Sequence." It is either stable and abstract, or unstable and concrete.
	    - **$D \approx 1$**: The package is in the "Zone of Pain" (highly stable/concrete, hard to change) or the "Zone of Uselessness" (highly abstract/unstable).
- MOOD (Metrics for object-oriented design)
	- "big picture"
	- metrics for a whole project 
	- AHF: attribute hiding factor - shows how many fields are private 
	- AIF (attribute inheritance factor) 
	- MIF (method inheritance factor)
	- CF (coupling factor)
		- represents actual couplings vs. maximum possible couplings 
		- higher = ball of mud 
	- MHF (method hiding factor)
		- measure how many methods are private 
	- PF (polymorphism factor)
		- ratio of actual overrides vs. possible overrides 
		- metric is skewed because i implement external interfaces (framework and `api` package)
## Visualizing 
### Chidamber Kemerer:
- Spider plot 
- Stacked bar chart for "package complexity"
- Scatter plot: show every class in a scatter plot; x=CBO, y=WMC
  helps identify "god classes" with high CBO and high WMC
### Complexity 
- CDF (cumulative distribution function) plot 
  xAxis = metric value (v(G))
  yAxis = percentage. then show the percentage of classes falling into this metric value 
- Complexity heat map (tree map)
  diagram with lots of rectangles 
  size of a rectangle = LOC of the method 
  color = determined by cyclomatic or cognitive complexity 
- standard box plot with whiskers 
  show every metric on the xAxis (as group)
  plot values for both apps and each group 
  include error bar
  most standard way to visualize 
### Dependency 
- Maybe use a dependency graph
	- probably very confusing to look at 
- dependency graph but without class names 
	- larger nodes = more dpt* (transitive dependencies)
	- meaning: large nodes have a big impact when being changed because many classes depend on them 
	- edge color = red for cyclic dependencies 
	- ES-CQRS application should show two decoupled clusters 
- heatmap 
	- x and y axis are the packages 
	- color of a cell = sum of PDcy 
	- illustrates coupling between packages 
- Boxplot showing transitive dependencies  
	- x axis = app 
	- y axis = Dcy* 
	- shows median / avg of transitive dependencies 
	- lower Dcy* = system is more modular and flexible 
### Martin 
- Main sequence graph: a **Scatter Plot** (A-I Graph).
	- **X-axis:** Instability (I)
	- **Y-axis:** Abstractness (A)
	- **The Line:** Draw a diagonal line from $(0, 1)$ to $(1, 0)$. This is the "Main Sequence."
	- **Plotting:** Map each package as a point. Use Blue dots for CRUD packages and Red dots for ES/CQRS packages.
	- Analysis: packages may be far from the main sequence = less flexible 
### MOOD 
- Spider plot 
	- overlay both app's values 
- Bar chart "quality indicator"
	- group AHF / MHF as "encapsulation" 
	- MIF / AIF as "inheritance"
	- CF / PF as "complexity"
