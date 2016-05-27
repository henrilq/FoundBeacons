 package com.jaalee.sdk;
 
 import android.os.Parcel;
 import android.os.Parcelable;

import com.jaalee.sdk.internal.Objects;

 import java.util.Date;

 /**
*Immutable representations of single beacon.
*Two beacons are considered equal if their proximity UUID, major and minor are equal.
*This mimics CLBeacon from iOS.  
*/
 public class Beacon
   implements Parcelable
 {
   private String proximityUUID;
   private String name;
   private String macAddress;
   private int major;
   private int minor;
   private transient int measuredPower;
   private transient int rssi;
   private transient int battLevel;
   private Date date;
     private double latitude;
     private double longitude;
   
   public static final Creator<Beacon> CREATOR = new Creator<Beacon>()
   {
     public Beacon createFromParcel(Parcel source) {
        return new Beacon(source);
     }
 
     public Beacon[] newArray(int size)
     {
        return new Beacon[size];
     }
    };

     public Beacon(){
         this.date = new Date();
     }
 
   public Beacon(String proximityUUID, String name, String macAddress, int major, int minor, int measuredPower, int rssi, int batt)
   {
      this.proximityUUID = Utils.normalizeProximityUUID(proximityUUID);
      this.name = name;
      this.macAddress = macAddress;
      this.major = major;
      this.minor = minor;
      this.measuredPower = measuredPower;
      this.rssi = rssi;
      this.battLevel = batt;
       this.date = new Date();
   }
 
   public String getProximityUUID()
   {
      return this.proximityUUID;
   }
 
   public String getName()
   {
      return this.name;
   }

     public void setName(String name) {
         this.name = name;
     }

     public String getMacAddress()
   {
      return this.macAddress;
   }

     public void setMacAddress(String macAddress) {
         this.macAddress = macAddress;
     }

     public int getMajor()
   {
      return this.major;
   }
 
   public int getMinor()
   {
      return this.minor;
   }
 
   public int getMeasuredPower()
   {
	   return this.measuredPower;
   }
 
   public int getRssi()
   {
	   return this.rssi;
   }

     public void setRssi(int rssi) {
         this.rssi = rssi;
     }

     public int getBattLevel()
   {
	   return this.battLevel;
   }

     public Date getDate() {
         return date;
     }

     public void setDate(Date date) {
         this.date = date;
     }

     public void setLatitude(double latitude) {
         this.latitude = latitude;
     }

     public double getLatitude() {
         return latitude;
     }

     public void setLongitude(double longitude) {
         this.longitude = longitude;
     }

     public double getLongitude() {
         return longitude;
     }

     public String toString()
   {
	   return Objects.toStringHelper(this).add("macAddress", this.macAddress).add("proximityUUID", this.proximityUUID).add("major", this.major).add("minor", this.minor).add("measuredPower", this.measuredPower)
			   .add("rssi", this.rssi)
			   .add("battLevel", this.battLevel)
			   .toString();
   }
 
   public boolean equals(Object o)
   {
	   if (this == o) return true;
	   if ((o == null) || (getClass() != o.getClass())) return false;
 
	   Beacon beacon = (Beacon)o;
 
	   if (this.major != beacon.major) return false;
	   if (this.minor != beacon.minor) return false;
	   return this.proximityUUID.equals(beacon.proximityUUID);
   }
 
   public int hashCode()
   {
	   int result = this.proximityUUID.hashCode();
	   result = 31 * result + this.major;
	   result = 31 * result + this.minor;
	   return result;
   }
 
   private Beacon(Parcel parcel)
   {
	   this.proximityUUID = parcel.readString();
	   this.name = parcel.readString();
	   this.macAddress = parcel.readString();
	   this.major = parcel.readInt();
	   this.minor = parcel.readInt();
	   this.measuredPower = parcel.readInt();
	   this.rssi = parcel.readInt();
	   this.battLevel = parcel.readInt();
   }
 
   public int describeContents()
   {
	   return 0;
   }
 
   public void writeToParcel(Parcel dest, int flags)
   {
	   dest.writeString(this.proximityUUID);
	   dest.writeString(this.name);
	   dest.writeString(this.macAddress);
	   dest.writeInt(this.major);
	   dest.writeInt(this.minor);
	   dest.writeInt(this.measuredPower);
	   dest.writeInt(this.rssi);
	   dest.writeInt(this.battLevel);
   }


 }
