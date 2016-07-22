package by.fly.util;

import by.fly.model.OrderStatus;
import by.fly.model.QOrderItem;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

public class Utils {

    public static final String[] READY_STATUS_NAMES = new String[]{OrderStatus.READY.name(), OrderStatus.PAID.name()};
    public static final OrderStatus[] READY_STATUSES = new OrderStatus[]{OrderStatus.READY, OrderStatus.PAID};

    public static final ZoneId ZONE = ZoneId.systemDefault();

    private Utils() {
        // utility
    }

    public static Pattern containsIgnoreCasePattern(String filter) {
        return Pattern.compile("(?i)(?=.*" + filter + ")");
    }

    public static String getCurrentMachineHardwareAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface address = NetworkInterface.getByInetAddress(localHost);
            return address != null ? Arrays.toString(address.getHardwareAddress()) : localHost.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static LocalDate toLocalDate(@Nullable Date date) {
        return Optional.ofNullable(date).map(d -> LocalDateTime.ofInstant(d.toInstant(), ZONE).toLocalDate()).orElse(null);
    }


    public static LocalDateTime toLocalDateTime(@Nullable Date date) {
        return Optional.ofNullable(date).map(d -> LocalDateTime.ofInstant(d.toInstant(), ZONE)).orElse(null);
    }

    public static Date toDate(@Nullable LocalDate date) {
        return Optional.ofNullable(date).map(d -> Date.from(d.atStartOfDay().atZone(ZONE).toInstant())).orElse(null);
    }

    public static Date toDate(@Nullable LocalDateTime dateTime) {
        return Optional.ofNullable(dateTime).map(d -> Date.from(d.atZone(ZONE).toInstant())).orElse(null);
    }

    public static Criteria readyOrdersCriteria() {
        return Criteria.where(QOrderItem.orderItem.status.getMetadata().getName()).in(READY_STATUS_NAMES);
    }

    public static BooleanExpression readyOrdersPredicate() {
        return QOrderItem.orderItem.status.in(READY_STATUSES);
    }

    public static Sort sortByOrderDeadLine() {
        return new Sort(Sort.Direction.DESC, QOrderItem.orderItem.deadLine.getMetadata().getName());
    }

}
