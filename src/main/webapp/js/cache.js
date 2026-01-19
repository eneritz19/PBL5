const cache = {
    set: (key, data) => localStorage.setItem(key, JSON.stringify(data)),
    get: (key) => {
        try { return JSON.parse(localStorage.getItem(key)); }
        catch { return null; }
    },
    remove: (key) => localStorage.removeItem(key)
};
