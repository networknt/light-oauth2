import React, {Component} from 'react';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles(theme => ({
    error: {
        color: '#ff0000',
    },
}));

function ErrorMessage(props) {
    const classes = useStyles();
    return (
        <div className={classes.error}>
            {props.error}
        </div>
    )
}

export default ErrorMessage;
