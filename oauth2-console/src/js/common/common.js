import { Service, Client, User } from '../main/views.js';

export const Views = {
    SERVICE: {
                name: "Services",
                path: "/services",
                dataType: "service",
                component: Service
        },
    CLIENT: {
                name: "Clients",
                path: "/clients",
                dataType: "client",
                component: Client
            },
    USER: {
                name: "Users",
                path: "/users",
                dataType: "user",
                component: User
          }
};
