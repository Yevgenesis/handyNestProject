package codezilla.handynestproject.generator;

import lombok.RequiredArgsConstructor;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.UUID;

@RequiredArgsConstructor
public class UuidTimeSequenceGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        long currentTime = System.currentTimeMillis();
        UUID uuid = UUID.randomUUID();
        return concatInHexFormat(currentTime, uuid);
    }

    private char[] concatInHexFormat(long currentTime, UUID uuid) {
        String uuidStr = uuid.toString().replace("-", "").toUpperCase();
        String millis = Long.toString(currentTime);
        String sequenceHex = uuidStr.substring(0, 16);
        String concatHex = millis + sequenceHex;

        return concatHex.toCharArray();
    }
}
