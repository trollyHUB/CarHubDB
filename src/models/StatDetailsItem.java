package models;

/**
 * Модель для детальной информации статистики
 */
public class StatDetailsItem {
    private int id;
    private String info1;
    private String info2;
    private String info3;
    private String date;

    public StatDetailsItem(int id, String info1, String info2, String info3, String date) {
        this.id = id;
        this.info1 = info1;
        this.info2 = info2;
        this.info3 = info3;
        this.date = date;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInfo1() {
        return info1;
    }

    public void setInfo1(String info1) {
        this.info1 = info1;
    }

    public String getInfo2() {
        return info2;
    }

    public void setInfo2(String info2) {
        this.info2 = info2;
    }

    public String getInfo3() {
        return info3;
    }

    public void setInfo3(String info3) {
        this.info3 = info3;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

