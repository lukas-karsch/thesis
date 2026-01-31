import {assertResponseIs201} from "./assert.js";

/**
 * Helper to shift a date by a specific number of days.
 * Automatically handles month-end (e.g., Dec 31 -> Jan 1).
 */
export const getOffsetDate = (baseDateStr, dayOffset) => {
    const date = new Date(baseDateStr);

    // Increment the day by the index
    date.setDate(date.getDate() + dayOffset);

    // Returns YYYY-MM-DD
    return date.toISOString().split('T')[0];
};

export const setTime = (newTime, http, targetHost) => {
    const res = http.post(`${targetHost}/actuator/date-time`, JSON.stringify(newTime), {
        headers: {
            "Content-Type": "application/json"
        }
    });
    assertResponseIs201(res)
}
