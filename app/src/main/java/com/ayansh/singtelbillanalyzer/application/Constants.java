/**
 * 
 */
package com.ayansh.singtelbillanalyzer.application;

/**
 * @author varun
 *
 */
public class Constants {

	private static boolean premiumVersion;
	private static String productTitle, productDescription, productPrice;
		
	public static String getPublicKey() {
		return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQE" +
                "AnvhF3vfjButC++bSw2wBYn8ggnBBGp+i/jfOLUO+X" +
                "0EgihIykPvFs2W6uydlGxuPZ3rutQCw4qN4yzCyy2c" +
                "vr5v3g9RCXZAIai00JsfwRcm/aNFFNCOjC89Wx88ach" +
                "fSluut6UKa6sqijPJ1pb8eLt6ZiVveYQsdnPirs87TM" +
                "pOaCfQ2sAWVdn0E3OOcuP9RE2rKUQ62IG3zqTSzmUZ7" +
                "M/ds4qPYFft16LgJCPkbbLwReNyQQI7YpxCM6IQxK5U" +
                "6zpP42TjgPHLePJZjbvXXgWu8L45i9HTG6o5zfjrkYA" +
                "ACNpKbTrlsEFpyyBF9QsDsZBkkZLutgSnAr39yB8DIN" +
                "wIDAQAB";
	}

	public static String getProductKey() {
		return "premium_content";
	}

	public static void setPremiumVersion(boolean premiumVersion) {
		Constants.premiumVersion = premiumVersion;
	}
	
	public static boolean isPremiumVersion(){
		return premiumVersion;
	}

	/**
	 * @return the productTitle
	 */
	public static String getProductTitle() {
		return productTitle;
	}

	/**
	 * @param productTitle the productTitle to set
	 */
	public static void setProductTitle(String productTitle) {
		Constants.productTitle = productTitle;
	}

	/**
	 * @return the productDescription
	 */
	public static String getProductDescription() {
		return productDescription;
	}

	/**
	 * @param productDescription the productDescription to set
	 */
	public static void setProductDescription(String productDescription) {
		Constants.productDescription = productDescription;
	}

	/**
	 * @return the productPrice
	 */
	public static String getProductPrice() {
		return productPrice;
	}

	/**
	 * @param productPrice the productPrice to set
	 */
	public static void setProductPrice(String productPrice) {
		Constants.productPrice = productPrice;
	}

}