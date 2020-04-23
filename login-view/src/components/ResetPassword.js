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


function ResetPassword(props) {
  let params = new URLSearchParams(props.location.search);
  //console.log("token = ", params.get('token'));
  //console.log("email = ", params.get('email'));

  const classes = useStyles();
  const [newPassword, setNewPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');

  const [error, setError] = useState('');

  const [email] = useState(params.get('email') == null ? '' : params.get('email'));
  const [token] = useState(params.get('token') == null ? '' : params.get('token'));

  const handleChangeNewPassword = e => {
    setNewPassword(e.target.value)
  };

  const handleChangePasswordConfirm = e => {
    setPasswordConfirm(e.target.value)
  };

  const handleSubmit = event => {
    //console.log("email = " + email + " token = " + token);
    event.preventDefault();
    const data = { email, token, newPassword, passwordConfirm };
    //console.log("data = ", data);
    const action = {
      'host': 'lightapi.net',
      'service': 'user',
      'action': 'resetPassword',
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
      setError("The password has been reset.");
    } catch (e) {
      const error = await e.json();
      console.log(error);
      setError(error.description);
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
            Reset Password
          </Typography>
          <ErrorMessage error={error}/>
          <form className={classes.form} noValidate onSubmit={handleSubmit}>
            <TextField
                variant="outlined"
                margin="normal"
                required
                disabled
                fullWidth
                id="email"
                label="Email"
                name="email"
                value={email}
                autoComplete="email"
                autoFocus
            />
            <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                name="newPassword"
                value={newPassword}
                label="New Password"
                type="password"
                id="newPassword"
                autoComplete="newPassword"
                onChange={handleChangeNewPassword}
            />
            <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                name="passwordConfirm"
                value={passwordConfirm}
                label="Password Confirm"
                type="password"
                id="passwordConfirm"
                autoComplete="passwordConfirm"
                onChange={handleChangePasswordConfirm}
            />
            <TextField
                name="token"
                value={token}
                type="hidden"
                id="token"
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

export default ResetPassword;