import {check} from 'k6';

export const checkResponseIs201 = res => check(res, {
    'is status 201': (r) => r.status === 201,
});

export const checkResponseIs200 = res => check(res, {
    'is status 201': (r) => r.status === 200,
});

export const assertResponseIs201 = res => {
    if (res.status !== 201) {
        throw new Error(`Status code assertion failed: was ${res.status}, expected 201}`)
    }
}
