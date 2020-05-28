package com.romoalamn.cauldron.setup;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static class CommonConfig {
        public final ForgeConfigSpec.BooleanValue followStateRecipe;
        public final ForgeConfigSpec.BooleanValue extraContent;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Configuration for Cauldron")
                    .push("cauldron");

            followStateRecipe = builder.comment("Does the cauldron require an appropriate block under it to create the potion?")
                    .translation("cauldron.config.follow_state")
                    .define("follow_state", true);
            extraContent = builder.comment("Enables extra content, such as potion sickness, etc. ")
                    .translation("cauldron.config.extra_content")
                    .define("extra_content", false);
            builder.pop();
        }
    }
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON_CONFIG;
    static {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON_CONFIG = specPair.getKey();
    }
}
