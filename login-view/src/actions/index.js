import { LOAD_MENU } from './types';

export function loadMenu(host) {
    return {
        type: LOAD_MENU,
        payload: host
    }
}
