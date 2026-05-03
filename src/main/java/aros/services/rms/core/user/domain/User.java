/* (C) 2026 */

package aros.services.rms.core.user.domain;

import aros.services.rms.core.area.domain.AreaId;
import java.time.Instant;
import java.util.List;

/** Domain model representing a user in the restaurant management system. */
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

  /**
   * Creates a new User instance.
   *
   * @param id the user identifier
   * @param document the user's document number
   * @param name the user's name
   * @param email the user's email
   * @param password the user's encoded password
   * @param address the user's address
   * @param phone the user's phone number
   * @param role the user's role
   * @param status the user's status
   * @param assignedAreas list of assigned area IDs
   */
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

  /**
   * Renames the user.
   *
   * @param newName the new name
   */
  public void rename(String newName) {
    this.name = newName;
  }

  /**
   * Changes the user's email.
   *
   * @param email the new email
   */
  public void changeEmail(UserEmail email) {
    this.email = email;
  }

  /**
   * Changes the user's password.
   *
   * @param password the new encoded password
   */
  public void changePassword(String password) {
    this.password = password;
  }

  /**
   * Updates the user's name.
   *
   * @param newName the new name
   */
  public void updateName(String newName) {
    this.name = newName;
  }

  /**
   * Updates the user's information.
   *
   * @param document the new document
   * @param name the new name
   * @param address the new address
   * @param phone the new phone
   */
  public void updateInfo(String document, String name, String address, String phone) {
    this.document = document;
    this.name = name;
    this.address = address;
    this.phone = phone;
  }

  /** Marks the user as active. */
  public void markAsActive() {
    this.status = UserStatus.ACTIVE;
  }

  /** Marks the user as error. */
  public void markAsError() {
    this.status = UserStatus.ERROR;
  }

  /** Marks the user as pending. */
  public void markAsPending() {
    this.status = UserStatus.PENDING;
  }

  /** Marks the user as inactive. */
  public void markAsInactive() {
    this.status = UserStatus.INACTIVE;
  }

  /**
   * Gets the user identifier.
   *
   * @return the user id
   */
  public UserId getId() {
    return id;
  }

  /**
   * Gets the user's document.
   *
   * @return the document string
   */
  public String getDocument() {
    return document;
  }

  /**
   * Gets the user's name.
   *
   * @return the name string
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the user's email.
   *
   * @return the email
   */
  public UserEmail getEmail() {
    return email;
  }

  /**
   * Gets the user's password.
   *
   * @return the encoded password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Gets the user's address.
   *
   * @return the address string
   */
  public String getAddress() {
    return address;
  }

  /**
   * Gets the user's phone.
   *
   * @return the phone string
   */
  public String getPhone() {
    return phone;
  }

  /**
   * Gets the user's role.
   *
   * @return the user role
   */
  public UserRole getRole() {
    return role;
  }

  /**
   * Gets the user's status.
   *
   * @return the user status
   */
  public UserStatus getStatus() {
    return status;
  }

  /**
   * Sets the user's status.
   *
   * @param status the new status
   */
  public void setStatus(UserStatus status) {
    this.status = status;
  }

  /**
   * Gets the assigned areas.
   *
   * @return list of area IDs
   */
  public List<AreaId> getAssignedAreas() {
    return assignedAreas;
  }

  /**
   * Gets whether the user is active.
   *
   * @return true if active
   */
  public boolean getActive() {
    return active;
  }

  /**
   * Sets whether the user is active.
   *
   * @param active the active flag
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Gets the deletion timestamp.
   *
   * @return the deleted at instant
   */
  public Instant getDeletedAt() {
    return deletedAt;
  }

  /**
   * Sets the deletion timestamp.
   *
   * @param deletedAt the deleted at instant
   */
  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }
}
