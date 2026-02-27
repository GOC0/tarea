package logic;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "tokens")
public class tokenS {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private String username;
    private LocalDateTime expira;

    public  tokenS(){}

    public tokenS(String token, String username, LocalDateTime expira) {
        this.token = token;
        this.username = username;
        this.expira = expira;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getExpira() {
        return expira;
    }

    public void setExpira(LocalDateTime expira) {
        this.expira = expira;
    }

}
