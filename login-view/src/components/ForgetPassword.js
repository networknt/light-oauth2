import React,  {useState} from 'react';
import Avatar from '@material-ui/core/Avatar';
import Button from '@material-ui/core/Button';
import CssBaseline from '@material-ui/core/CssBaseline';
import TextField from '@material-ui/core/TextField';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import ErrorMessage from './ErrorMessage';

const useStyles = makeStyles(theme => ({
  '@global': {
    body: {
      backgroundColor: theme.palette.common.white,
    },
  },
  paper: {
    marginTop: theme.spacing(8),
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
  },
  avatar: {
    margin: theme.spacing(1),
    backgroundColor: theme.palette.secondary.main,
  },
  form: {
    width: '100%', // Fix IE 11 issue.
    marginTop: theme.spacing(1),
  },
  submit: {
    margin: theme.spacing(3, 0, 2),
  },
}));


function ForgetPassword() {
  const classes = useStyles();
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');

  const handleChangeEmail = e => {
    setEmail(e.target.value)
  };

  const handleSubmit = event => {
    //console.log("email = ", email);
    event.preventDefault();
    const data = { email };
    //console.log("data = ", data);
    const action = {
      'host': 'lightapi.net',
      'service': 'user',
      'action': 'forgetPassword',
      'version': '0.1.0',
      'data': data
    };
    const headers = {
      'Content-Type': 'application/json'
    };
    submitForm('/portal/command', headers, action);
  };

  const submitForm = async (url, headers, action) => {
    try {
      const response = await fetch(url, { method: 'POST', body: JSON.stringify(action), headers});
      if (!response.ok) {
        throw response;
      }
      const data = await response.json();
      //console.log(data);
      setError("An Email has been sent.");
    } catch (e) {
      const error = await e.json();
      console.log(error);
      setError(error.description);
      //props.history.push();
    }
  };

  return (
      <Container component="main" maxWidth="xs">
        <CssBaseline />
        <div className={classes.paper}>
          <Avatar className={classes.avatar}>
            <LockOutlinedIcon />
          </Avatar>
          <Typography component="h1" variant="h5">
            Forget Password
          </Typography>
          <ErrorMessage error={error}/>
          <form className={classes.form} noValidate onSubmit={handleSubmit}>
            <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                id="email"
                label="Email"
                name="email"
                value={email}
                autoComplete="email"
                autoFocus
                onChange={handleChangeEmail}
            />
            <Button
                type="submit"
                fullWidth
                variant="contained"
                color="primary"
                className={classes.submit}
            >
              Submit
            </Button>
          </form>
        </div>
      </Container>
  );
}

export default ForgetPassword;