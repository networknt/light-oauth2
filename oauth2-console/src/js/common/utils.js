import axios from 'axios';

export default class Utils{
    static isEmpty(s){
        return !s || 0===s.length;
    }

    static capitalize(s){
        return s && s[0].toUpperCase() + s.slice(1).toLowerCase();
    }

    static replace(str, s, r){
        return str && str.replace(s,r);
    }

    /*
    * added a 'i-' prefix to make sure the id does not start with number.
    * see: css-escapes#Leading digits
    */
    static toItemId(id){
        return 'i-'+id;
    }
    
    /*
    * added a 'v-' prefix to make sure the id does not start with number.
    * see: css-escapes#Leading digits
    */
    static toViewerId(id){
        return 'v-'+id;
    }

    static clean(obj){
        Object.keys(obj).forEach(function(key) {
            if (obj[key] && typeof obj[key] === 'object') Utils.clean(obj[key])
            else if (obj[key] == null) delete obj[key]
        });

        return obj;
    }

    static createAxiosClient(){
        return axios.create({ 
                validateStatus: function (status) {
                                    return status === 200;
                                },
                Origin:window.location.origin     // set 'Origin' header to meet CORS requirements
        });
    }

    static toFormData(obj){
        var formData = new FormData();

        for ( var key in obj ) {
            formData.append(key, obj[key]);
        }

        return formData;
    }
} 
