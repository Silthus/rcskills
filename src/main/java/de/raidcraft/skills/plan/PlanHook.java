package de.raidcraft.skills.plan;

import com.djrapitops.plan.capability.CapabilityService;
import com.djrapitops.plan.extension.ExtensionService;

public class PlanHook {

    public void hookIntoPlan() {

        if (!areAllCapabilitiesAvailable()) return;
        registerDataExtension();
        listenForPlanReloads();
    }

    private boolean areAllCapabilitiesAvailable() {

        return true;
    }

    private void registerDataExtension() {
        try {
            ExtensionService.getInstance().register(new RCSkillsDataExtension());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForPlanReloads() {
        CapabilityService.getInstance().registerEnableListener(
                isPlanEnabled -> {
                    // Register DataExtension again
                    if (isPlanEnabled) registerDataExtension();
                }
        );
    }
}
