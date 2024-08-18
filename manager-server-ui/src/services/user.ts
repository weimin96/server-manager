import axios from 'axios';

class User {
  static login(username: string, password: string) {
    return axios.post(
      'api/login',
      { username, password },
      { headers: { 'Content-Type': 'application/json' } },
    );
  }
}

export default User;
