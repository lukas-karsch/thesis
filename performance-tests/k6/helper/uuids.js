export function getUuidForVu(VU, uuids) {
    if (!uuids[VU]) {
        uuids[VU] = crypto.randomUUID();
    }
    return uuids[VU];
}
