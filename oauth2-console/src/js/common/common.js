import { Service, Client, User } from '../main/views.js';

export const Views = [
    {
        name: "Services",
        path: "/services",
        component: Service
    },
    {
        name: "Clients",
        path: "/clients",
        component: Client
    },
    {
        name: "Users",
        path: "/users",
        component: User
    }
];
