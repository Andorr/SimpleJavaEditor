package LogIn;

import java.io.Serializable;

public class UserID implements Serializable {
    String userName;
    String password;

    public UserID(String userName,String password){
        this.userName = userName;
        this.password = password;
    }

    public boolean equals(String userName,String password) {
        return (this.userName.equals(userName) && this.password.equals(password));
    }
}
