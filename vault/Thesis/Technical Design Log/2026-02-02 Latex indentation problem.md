#latex
## Problem
after listings, paragraph indentation stops working. 
## Cause
\captionof usage outside a box / environment; sets parindent to 0 
## Solution
Wrap \captionof in a block by using curly brackets {}. Make sure label is in the same block! 

Before: 
```tex
\begin{lstlisting}[language=JavaScript]
listing here
\end{lstlisting}
\captionof{lstlisting}[Short caption]{Long caption}
\label{lst:create-course-k6-script}
```
After:
```tex
\begin{lstlisting}[language=JavaScript]
listing here
\end{lstlisting}
{
\captionof{lstlisting}[Short caption]{Veeery Long caption}
\label{lst:create-course-k6-script}
}
```
