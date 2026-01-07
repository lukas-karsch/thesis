export const getVUS = ((ENV) => {
    const vus = Number(ENV.VUs);
    if (Number.isNaN(vus) || vus <= 0) {
        console.log("No -e value set for VUs, defaulting to 20")
        return 20;
    }
    return vus;
});
