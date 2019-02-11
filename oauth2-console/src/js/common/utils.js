export default class Utils{
    static isEmpty(s){
        return !s || 0===s.length;
    }

    static capitalize(s){
        return s && s[0].toUpperCase() + s.slice(1).toLowerCase();
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
} 
