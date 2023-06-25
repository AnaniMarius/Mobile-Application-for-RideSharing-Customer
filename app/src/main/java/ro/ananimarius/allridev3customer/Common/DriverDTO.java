package ro.ananimarius.allridev3customer.Common;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

public class DriverDTO {
    private String firstName;
    private String lastName;
    private String phone;
    private String googleId;
    private double latitude;
    private double longitude;
    private String car;
    private float currentRating;
    private BigDecimal currentRidePrice;
    private Double currentRideTotalDistance;
    private BigDecimal currentRideTotalTime;

    public Double getCurrentRideTotalDistance() {
        return currentRideTotalDistance;
    }

    public void setCurrentRideTotalDistance(Double currentRideTotalDistance) {
        this.currentRideTotalDistance = currentRideTotalDistance;
    }

    public BigDecimal getCurrentRideTotalTime() {
        return currentRideTotalTime;
    }

    public void setCurrentRideTotalTime(BigDecimal currentRideTotalTime) {
        this.currentRideTotalTime = currentRideTotalTime;
    }

    public BigDecimal getCurrentRidePrice() {
        return currentRidePrice;
    }

    public void setCurrentRidePrice(BigDecimal currentRidePrice) {
        this.currentRidePrice = currentRidePrice;
    }
    public DriverDTO(String firstName, String lastName, String phone, String googleId, double latitude, double longitude, String car, float currentRating) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.googleId = googleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.car = car;
        this.currentRating = currentRating;
    }

    // Parcelable implementation
    protected DriverDTO(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
    }

    public static final Parcelable.Creator<DriverDTO> CREATOR = new Parcelable.Creator<DriverDTO>() {
        @Override
        public DriverDTO createFromParcel(Parcel in) {
            return new DriverDTO(in);
        }

        @Override
        public DriverDTO[] newArray(int size) {
            return new DriverDTO[size];
        }
    };

    public DriverDTO() {

    }
    public DriverDTO(double latitude, double longitude){
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public float getCurrentRating() {
        return currentRating;
    }

    public void setCurrentRating(float currentRating) {
        this.currentRating = currentRating;
    }
}

