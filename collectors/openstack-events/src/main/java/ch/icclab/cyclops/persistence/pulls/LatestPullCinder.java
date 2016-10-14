package ch.icclab.cyclops.persistence.pulls;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description:
 */
@Entity
public class LatestPullCinder{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    public LatestPullCinder() {}

    public LatestPullCinder(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    private Long timeStamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
