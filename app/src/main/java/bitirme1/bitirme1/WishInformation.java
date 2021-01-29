package bitirme1.bitirme1;

public class WishInformation {

    public String Key;
    public String Name;
    public String Brand;
    public String Model;
    public String Additionalinfo;
    public String LinkOfProduct;
    public String CountryFrom;
    public String CountryTo;
    public String CountryFromTo;
    public String ImageDownloadLink;
    public String UserUid;

    public WishInformation() {

    }

    public WishInformation(String key,String name, String brand, String model, String additionalinfo, String linkOfProduct,String countryFrom, String countryTo,String countryFromTo, String imageDownloadLink,String userUid) {
        Key = key;
        Name = name;
        Brand = brand;
        Model = model;
        Additionalinfo = additionalinfo;
        LinkOfProduct = linkOfProduct;
        CountryFrom = countryFrom;
        CountryTo = countryTo;
        CountryFromTo = countryFromTo;
        ImageDownloadLink = imageDownloadLink;
        UserUid = userUid;
    }
}
