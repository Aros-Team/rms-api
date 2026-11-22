/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Entity representing the single business profile row. */
@Entity
@Table(name = "business_profile")
public class BusinessProfileEntity {

  @Id private Long id;

  @Column(name = "restaurant_name", nullable = false, length = 120)
  private String restaurantName;

  @Column(name = "legal_name", length = 160)
  private String legalName;

  @Column(name = "tax_id", length = 40)
  private String taxId;

  @Column(length = 200)
  private String address;

  @Column(length = 30)
  private String phone;

  @Column(length = 120)
  private String email;

  @Column(nullable = false, length = 60)
  private String timezone;

  @Column(nullable = false, length = 20)
  private String locale;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRestaurantName() {
    return restaurantName;
  }

  public void setRestaurantName(String restaurantName) {
    this.restaurantName = restaurantName;
  }

  public String getLegalName() {
    return legalName;
  }

  public void setLegalName(String legalName) {
    this.legalName = legalName;
  }

  public String getTaxId() {
    return taxId;
  }

  public void setTaxId(String taxId) {
    this.taxId = taxId;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
