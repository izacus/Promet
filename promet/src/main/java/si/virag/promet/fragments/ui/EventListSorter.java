package si.virag.promet.fragments.ui;

import java.util.Comparator;

import si.virag.promet.api.model.PrometEvent;

public class EventListSorter implements Comparator<PrometEvent> {
    @Override
    public int compare(PrometEvent lhs, PrometEvent rhs) {
        // Sort by roadType first
        if (lhs.eventGroup != rhs.eventGroup) {
            if (lhs.eventGroup == null)
                return rhs.eventGroup == null ? 0 : 1;

            if (rhs.eventGroup == null)
                return -1;

            return lhs.eventGroup.compareTo(rhs.eventGroup);
        }

        // Now sort by what kind of event it is
        if (!rhs.causeSl.equals(lhs.causeSl)) {
            if (lhs.causeSl.equalsIgnoreCase("nesreča"))
                return -1;
            if (rhs.causeSl.equalsIgnoreCase("nesreča"))
                return 1;

            if (lhs.causeSl.equalsIgnoreCase("zastoj"))
                return -1;
            if (rhs.causeSl.equalsIgnoreCase("zastoj"))
                return 1;

            if (lhs.causeSl.equalsIgnoreCase("izredni dogodek"))
                return -1;
            if (rhs.causeSl.equalsIgnoreCase("izredni dogodek"))
                return 1;

            return lhs.causeSl.compareTo(rhs.causeSl);
        }

        // Failing that, sort by date
        return lhs.roadNameSl.compareTo(rhs.roadNameSl);
    }
}
