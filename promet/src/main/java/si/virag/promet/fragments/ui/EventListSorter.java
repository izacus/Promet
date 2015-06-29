package si.virag.promet.fragments.ui;

import rx.functions.Func2;
import si.virag.promet.api.model.PrometEvent;

public class EventListSorter implements Func2<PrometEvent, PrometEvent, Integer> {
    @Override
    public Integer call(PrometEvent lhs, PrometEvent rhs)
    {
        // Sort by roadType first
        if (lhs.eventGroup != rhs.eventGroup) {
            if (lhs.eventGroup == null)
                return rhs.eventGroup == null ? 0 : 1;

            if (rhs.eventGroup == null)
                return -1;

            return lhs.eventGroup.compareTo(rhs.eventGroup);
        }

        // Now sort by what kind of event it is
        if (!rhs.cause.equals(lhs.cause)) {
            if (lhs.cause.equalsIgnoreCase("nesreča"))
                return -1;
            if (rhs.cause.equalsIgnoreCase("nesreča"))
                return 1;

            if (lhs.cause.equalsIgnoreCase("zastoj"))
                return -1;
            if (rhs.cause.equalsIgnoreCase("zastoj"))
                return 1;

            if (lhs.cause.equalsIgnoreCase("izredni dogodek"))
                return -1;
            if (rhs.cause.equalsIgnoreCase("izredni dogodek"))
                return 1;

            return lhs.cause.compareTo(rhs.cause);
        }

        // Failing that, sort by date
        return lhs.roadName.compareTo(rhs.roadName);
    }
}
