import React, {useState} from 'react';
import Avatar from '@material-ui/core/Avatar';
import Button from '@material-ui/core/Button';
import CssBaseline from '@material-ui/core/CssBaseline';
import TextField from '@material-ui/core/TextField';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';

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


function App() {
  const classes = useStyles();

  let search = window.location.search;
  let params = new URLSearchParams(search);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(false);
  const [state] = useState(params.get('state') == null ? '' : params.get('state'));
  const [clientId] = useState(params.get('client_id') == null ? '' : params.get('client_id'));
  const [userType] = useState(params.get('user_type') == null ? '' : params.get('user_type'));
  const [redirectUri] = useState(params.get('redirect_uri') == null ? '' : params.get('redirect_uri'));

  const handleChangeUsername = e => {
    setUsername(e.target.value)
  };

  const handleChangePassword = e => {
    setPassword(e.target.value)
  };

  const handleChangeRemember = e => {
    setRemember(e.target.value)
  };

  const handleSubmit = event => {
    console.log("username = " + username + " password = " + password + " remember = " + remember);
    console.log("state = " + state + " clientId = " + clientId + " userType = " + userType + " redirectUri = " + redirectUri);
    event.preventDefault();

    let data = {
      j_username: username,
      j_password: password,
      state: state,
      client_id: clientId,
      user_type: userType,
      redirect_uri: redirectUri
    };

    const formData = Object.keys(data).map(key => encodeURIComponent(key) + '=' + encodeURIComponent(data[key])).join('&');

    console.log(formData);
    // const formData = new URLSearchParams();
    // formData.append('j_username', {username});
    // formData.append('j_password', {password});

    // var formData = new FormData();
    // for (var k in data) {
    //   formData.append(k, data[k]);
    // }

    fetch("/oauth2/code", {
      method: 'POST',
      redirect: 'follow',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: formData
    })
    .then(response => {
      // HTTP redirect.
      if (response.ok && response.redirected) {
        window.location.href = response.url;
      } else {
        throw Error(response.statusText);
      }
    })
    .catch(error => console.log("error=", error));
  };


  return (
      <Container component="main" maxWidth="xs">
        <CssBaseline />
        <div className={classes.paper}>
          <Avatar className={classes.avatar}>
            <LockOutlinedIcon />
          </Avatar>
          <Typography component="h1" variant="h5">
            Sign in
          </Typography>
          <form className={classes.form} noValidate onSubmit={handleSubmit}>
            <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                id="j_username"
                label="User Id"
                name="j_username"
                value={username}
                autoComplete="username"
                autoFocus
                onChange={handleChangeUsername}
            />
            <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                name="j_password"
                value={password}
                label="Password"
                type="password"
                id="j_password"
                autoComplete="password"
                onChange={handleChangePassword}
            />
            <TextField
                name="state"
                value={state}
                type="hidden"
                id="state"
            />
            <TextField
                name="client_id"
                value={clientId}
                type="hidden"
                id="client_id"
            />
            <TextField
                name="user_type"
                value={userType}
                type="hidden"
                id="user_type"
            />
            <TextField
                name="redirect_uri"
                value={redirectUri}
                type="hidden"
                id="redirect_uri"
            />
            <FormControlLabel
                control={<Checkbox value="remember" color="primary" />}
                label="Remember me"
                onChange={handleChangeRemember}
            />
            <Button
                type="submit"
                fullWidth
                variant="contained"
                color="primary"
                className={classes.submit}
            >
              Sign In
            </Button>
          </form>
        </div>
      </Container>
  );
}

export default App;
