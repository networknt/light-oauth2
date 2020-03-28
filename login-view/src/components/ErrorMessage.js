import React from 'react';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles(theme => ({
    error: {
        color: '#ff0000',
    },
}));

function ErrorMessage(props) {
    const classes = useStyles();
    return (
        <div className={classes.error}  dangerouslySetInnerHTML={ createMarkup(props.error) } />
    )
}

function createMarkup(error) {
  return {__html: error};
}

export default ErrorMessage;
