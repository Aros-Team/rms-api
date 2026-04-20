/* (C) 2026 */
package aros.services.rms.core.user.domain;

import aros.services.rms.core.area.domain.AreaId;
import java.time.Instant;
import java.util.List;

public class User {
  private UserId id;
  private String document;
  private String name;
  private UserEmail email;
  private String password;
  private String address;
  private String phone;
  private UserRole role;
  private UserStatus status;
  private List<AreaId> assignedAreas;
  private boolean active;
  private Instant deletedAt;

  public User(
      UserId id,
      String document,
      String name,
      UserEmail email,
      String password,
      String address,
      String phone,
      UserRole role,
      UserStatus status,
      List<AreaId> assignedAreas) {
    this.id = id;
    this.document = document;
    this.name = name;
    this.email = email;
    this.password = password;
    this.address = address;
    this.phone = phone;
    this.role = role;
    this.status = status;
    this.assignedAreas = assignedAreas;
    this.active = true;
    this.deletedAt = null;
  }

  public void rename(String newName) {
    this.name = newName;
  }

  public void changeEmail(UserEmail email) {
    this.email = email;
  }

  public void changePassword(String password) {
    this.password = password;
  }

  public void changeName(String name) {
    this.name = name;
  }

  public void updateInfo(String name, String address, String phone) {
    this.name = name;
    this.address = address;
    this.phone = phone;
  }

  public void markAsActive() {
    this.status = UserStatus.ACTIVE;
  }

  public void markAsError() {
    this.status = UserStatus.ERROR;
  }

  public void markAsPending() {
    this.status = UserStatus.PENDING;
  }

  public void markAsInactive() {
    this.status = UserStatus.INACTIVE;
  }

  public UserId getId() {
    return id;
  }

  public String getDocument() {
    return document;
  }

  public String getName() {
    return name;
  }

  public UserEmail getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getAddress() {
    return address;
  }

  public String getPhone() {
    return phone;
  }

  public UserRole getRole() {
    return role;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public List<AreaId> getAssignedAreas() {
    return assignedAreas;
  }

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }
}
