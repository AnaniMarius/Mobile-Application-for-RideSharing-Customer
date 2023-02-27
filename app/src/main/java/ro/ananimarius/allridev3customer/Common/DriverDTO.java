package ro.ananimarius.allridev3customer.Common;

public class DriverDTO {
    private String firstName;
    private String lastName;
    private String phone;
    private String googleId;
    private double latitude;
    private double longitude;
    private String car;
    private float currentRating;

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

