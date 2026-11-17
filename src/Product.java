import java.util.Objects;

public class Product {
    private String ID;
    private String name;
    private String description;
    private double cost;

    //Constructor

    public Product(String ID, String name, String description, double cost){
        this.ID = ID;
        this.name = name;
        this.description = description;
        this.cost = cost;
    }

    //Setters and Getters

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCost() {return cost; }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "Product{" +
                "ID='" + ID + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cost=" + cost +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(ID, product.ID) && Objects.equals(name, product.name) && Objects.equals(description, product.description) && Objects.equals(cost, product.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, name, description, cost);
    }

    public String toCSV() {
        String ret = ID + ", " + name + ", " + description + ", " + cost;
        return ret;
    }

    public String toPaddedCSV() {
        String ret = paddingID() + ", " + paddingName() + ", " + paddingDescription() + ", " + paddingCost();
        return ret;
    }

    public String toJSON() {
        String safeID = this.ID.replace("\"", "\\\"");
        String safeFirstName = this.name.replace("\"", "\\\"");
        String safeLastName = this.description.replace("\"", "\\\"");
        String ret = "{\"ID\":\"" + safeID + "\"," +
                "\"name\":\"" + safeFirstName + "\"," +
                "\"description\":\"" + safeLastName + "\"," +
                "\"cost\":" + cost + "}";
        return ret;
    }

    public String toXML() {
        String retString = "";
        retString = "<Product>";
        retString += "<ID>" + ID + "</ID>";
        retString += "<name>" + this.name + "</name>";
        retString += "<description>" + this.description + "</description>";
        retString += "<cost>" + this.cost + "</cost></Product>";
        return retString;
    }

    public String paddingName() {
        return String.format("%-35s", name);
    }

    public String paddingDescription() {
        return String.format("%-75s", description);
    }

    public String paddingID() {
        return String.format("%-6s", ID);
    }

    public String paddingCost() {
        String formatted = String.format(java.util.Locale.US, "%-8.2f", cost);
        if (formatted.length() > 8) {
            return formatted.substring(0, 8);
        }
        return formatted;
    }

}
