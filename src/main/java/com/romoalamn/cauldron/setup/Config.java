package com.romoalamn.cauldron.setup;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static class CommonConfig {
        public final ForgeConfigSpec.BooleanValue followStateRecipe;
        public final ForgeConfigSpec.BooleanValue extraContent;

        public final ForgeConfigSpec.IntValue maxUses;
        public final ForgeConfigSpec.BooleanValue doEffectsChangeUses;

        public final ForgeConfigSpec.BooleanValue ignoreInstants;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Configuration for Cauldron")
                    .push("cauldron");

            followStateRecipe = builder.comment("Does the cauldron require an appropriate block under it to create the potion?")
                    .translation("cauldron.config.follow_state")
                    .define("follow_state", true);
            extraContent = builder.comment("Enables extra content, such as potion sickness, etc. ")
                    .translation("cauldron.config.extra_content")
                    .define("extra_content", false);

            builder.comment("Configuration for extra content")
                    .push("extra");

            maxUses = builder.comment("How many uses does a potion effect have?")
                    .translation("cauldron.config.extra.max_uses")
                    .defineInRange("mas_uses", 100, 0, 1000);

            doEffectsChangeUses = builder.comment("Do the potion effects determine how many uses " +
                    "are given to a potion enchantment\n If this is true, having more effects would reduce the usages" +
                    "you have of the enchantment.")
                    .translation("cauldron.config.extra.factor_effects")
                    .define("factor_effects", true);
            ignoreInstants = builder.comment("Are Instant potions applied to the enchantment. If true, these effects are ignored.")
                    .translation("cauldron.config.extra.ignore_instant")
                    .define("ignore_instants", true);
            builder.pop(2);
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
