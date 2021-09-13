package io.branch.referral.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan Groth on 12/21/16.
 */
public class CommerceEvent {
    private Double revenue;
    private CurrencyType currencyType;
    private String transactionID;
    private Double shipping;
    private Double tax;
    private String coupon;
    private String affiliation;
    private List<Product> products;

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(CurrencyType currency) {
        this.currencyType = currency;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public Double getShipping() {
        return shipping;
    }

    public void setShipping(Double shipping) {
        this.shipping = shipping;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public String getCoupon() {
        return coupon;
    }

    public void setCoupon(String coupon) {
        this.coupon = coupon;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void addProduct(Product product) {
        if (this.products == null) {
            this.products = new ArrayList<>();
        }
        this.products.add(product);
    }

    public CommerceEvent() {
    }

    public CommerceEvent(Double revenue, CurrencyType currency, String transactionID, Double shipping, Double tax, String coupon, String affiliation, List<Product> products) {
        this.revenue = revenue;
        this.currencyType = currency;
        this.transactionID = transactionID;
        this.shipping = shipping;
        this.tax = tax;
        this.coupon = coupon;
        this.affiliation = affiliation;
        this.products = products;
    }

    public CommerceEvent(Double revenue, CurrencyType currency, String transactionID, Double shipping, Double tax, String coupon, String affiliation, Product product) {
        this.revenue = revenue;
        this.currencyType = currency;
        this.transactionID = transactionID;
        this.shipping = shipping;
        this.tax = tax;
        this.coupon = coupon;
        this.affiliation = affiliation;
        this.products = new ArrayList<>();
        this.products.add(product);
    }

    public JSONObject getCommerceJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("revenue", this.revenue);
            jsonObject.put("currency", this.currencyType);
            jsonObject.put("transaction_id", this.transactionID);
            jsonObject.put("shipping", this.shipping);
            jsonObject.put("tax", this.tax);
            jsonObject.put("coupon", this.coupon);
            jsonObject.put("affiliation", this.affiliation);
            if ( getProducts() != null ) jsonObject.put("products", getProducts());
        } catch (JSONException e) {

        }

        return jsonObject;
    }

    public List<JSONObject> getProducts() {
        if (this.products == null) {
            return null;
        }
        List<JSONObject> products = new ArrayList<>();
        for (Product p : this.products) {
            products.add(p.getProductJSONObject());
        }
        return products;
    }
}
