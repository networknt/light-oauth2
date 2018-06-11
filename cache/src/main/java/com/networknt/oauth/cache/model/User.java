package com.networknt.oauth.cache.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import io.swagger.annotations.ApiModelProperty;

import java.io.IOException;
import java.util.Objects;


public class User implements IdentifiedDataSerializable {
  private String userId = null;

  /**
   * user type
   */
  public enum UserTypeEnum {
    ADMIN("admin"),
    
    EMPLOYEE("employee"),
    
    CUSTOMER("customer"),
    
    PARTNER("partner");

    private final String value;

    UserTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static UserTypeEnum fromValue(String text) {
      for (UserTypeEnum b : UserTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  private UserTypeEnum userType = null;

  private String firstName = null;

  private String lastName = null;

  private String email = null;

  private String password = null;

  private String passwordConfirm = null;

  private String roles = null;

  public User userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "a unique id")
  @JsonProperty("userId")
  public String getUserId() {
    return userId;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }

  public User userType(UserTypeEnum userType) {
    this.userType = userType;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "user type")
  @JsonProperty("userType")
  public UserTypeEnum getUserType() {
    return userType;
  }
  public void setUserType(UserTypeEnum userType) {
    this.userType = userType;
  }

  public User firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "first name")
  @JsonProperty("firstName")
  public String getFirstName() {
    return firstName;
  }
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public User lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "last name")
  @JsonProperty("lastName")
  public String getLastName() {
    return lastName;
  }
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public User email(String email) {
    this.email = email;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "email address")
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }

  public User password(String password) {
    this.password = password;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "password")
  @JsonProperty("password")
  public String getPassword() {
    return password;
  }
  public void setPassword(String password) {
    this.password = password;
  }

  public User passwordConfirm(String passwordConfirm) {
    this.passwordConfirm = passwordConfirm;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "password confirm")
  @JsonProperty("passwordConfirm")
  public String getPasswordConfirm() {
    return passwordConfirm;
  }
  public void setPasswordConfirm(String passwordConfirm) {
    this.passwordConfirm = passwordConfirm;
  }


  public User roles(String roles) {
    this.roles = roles;
    return this;
  }


  @ApiModelProperty(example = "null", value = "user roles")
  @JsonProperty("roles")
  public String getRoles() {
    return roles;
  }
  public void setRoles(String roles) {
    this.roles = roles;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(userId, user.userId) &&
        Objects.equals(userType, user.userType) &&
        Objects.equals(firstName, user.firstName) &&
        Objects.equals(lastName, user.lastName) &&
        Objects.equals(email, user.email) &&
        Objects.equals(password, user.password) &&
        Objects.equals(passwordConfirm, user.passwordConfirm) &&
        Objects.equals(roles, user.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, userType, firstName, lastName, email, password, passwordConfirm, roles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class User {\n");
    
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    userType: ").append(toIndentedString(userType)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    passwordConfirm: ").append(toIndentedString(passwordConfirm)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  public User() {

  }

  @Override
  public void readData(ObjectDataInput in) throws IOException {
    this.userId = in.readUTF();
    this.userType = User.UserTypeEnum.fromValue(in.readUTF());
    this.firstName = in.readUTF();
    this.lastName = in.readUTF();
    this.email = in.readUTF();
    this.password = in.readUTF();
    this.roles = in.readUTF();
  }

  @Override
  public void writeData(ObjectDataOutput out) throws IOException {
    out.writeUTF(this.userId);
    out.writeUTF(this.userType.toString());
    out.writeUTF(this.firstName);
    out.writeUTF(this.lastName);
    out.writeUTF(this.email);
    out.writeUTF(this.password);
    out.writeUTF(this.roles);
  }

  @JsonIgnore
  @Override
  public int getFactoryId() {
    return UserDataSerializableFactory.ID;
  }

  @JsonIgnore
  @Override
  public int getId() {
    return UserDataSerializableFactory.USER_TYPE;
  }


}

