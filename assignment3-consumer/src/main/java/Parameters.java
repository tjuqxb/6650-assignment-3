import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameters {
    /**
     * predefined parameter xml mapping file
     * arg0: number of threads to run
     */
    @XmlElement
    Integer numThreads;
    String  service;
    String  RedisIP;
}
