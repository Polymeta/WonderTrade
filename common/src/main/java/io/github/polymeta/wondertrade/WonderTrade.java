package io.github.polymeta.wondertrade;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import io.github.polymeta.wondertrade.commands.RegeneratePool;
import io.github.polymeta.wondertrade.commands.Trade;
import io.github.polymeta.wondertrade.configuration.BaseConfig;
import io.github.polymeta.wondertrade.configuration.Pool;
import kotlin.Unit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;


public class WonderTrade {
    public static final String MOD_ID = "wondertrade";
    public static final MutableComponent PREFIX = Component.literal("[").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("Wonder").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("Trade").withStyle(ChatFormatting.RED))
            .append(Component.literal("]").withStyle(ChatFormatting.GRAY));
    public static BaseConfig config;
    public static Pool pool;
    public static ScheduledThreadPoolExecutor scheduler;
    public static ForkJoinPool worker;
    private static final Random rng = new Random();
    private static final Logger logger = LogManager.getLogger();

    public static void init() {
        logger.info("WonderTrade by Polymeta starting up!");
        scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName("WonderTrade Thread");
            return thread;
        });
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        worker = new ForkJoinPool(16, new WorkerThreadFactory(), new ExceptionHandler(), false);
        //load config and pool and message configuration
        loadConfig();
        loadPool();

        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            RegeneratePool.register(dispatcher);
            Trade.register(dispatcher);
        });
        CobblemonEvents.SERVER_STARTED.subscribe(Priority.NORMAL, minecraftServer -> {
            if(WonderTrade.pool.pokemon.isEmpty()) {
                logger.info("Regenerating pool as it is empty");
                WonderTrade.regeneratePool(WonderTrade.config.poolSize);
            }
            return Unit.INSTANCE;
        });
        //TODO register message ticks
    }

    public static void regeneratePool(int size) {
        var randomProp = PokemonProperties.Companion.parse("species=random", " ", "=");
        pool.pokemon.clear();
        for (int i = 0; i < size; i++) {
            randomProp.setLevel(rng.nextInt(1, Cobblemon.config.getMaxPokemonLevel()));
            var pokemon = randomProp.create();
            pool.pokemon.add(pokemon.createPokemonProperties(PokemonPropertyExtractor.Companion.getALL()).asString(" "));
        }
        savePool();
    }

    private static void loadConfig() {
        var configFile = new File("config/wondertrade/main.json");
        configFile.getParentFile().mkdirs();

        // Check config existence and load if it exists, otherwise create default.
        if (configFile.exists()) {
            try {
                var fileReader = new FileReader(configFile);
                config = BaseConfig.GSON.fromJson(fileReader, BaseConfig.class);
                fileReader.close();
            } catch (Exception e) {
                logger.error("Failed to load the config! Using default config as fallback");
                e.printStackTrace();
                config = new BaseConfig();
            }

        } else {
            config = new BaseConfig();
        }

        saveConfig();
    }

    private static void loadPool() {
        var configFile = new File("config/wondertrade/pool.json");
        configFile.getParentFile().mkdirs();

        // Check config existence and load if it exists, otherwise create default.
        if (configFile.exists()) {
            try {
                var fileReader = new FileReader(configFile);
                pool = BaseConfig.GSON.fromJson(fileReader, Pool.class);
                fileReader.close();
            } catch (Exception e) {
                logger.error("Failed to load pre-existing wondertrade pool! Removing broken file...");
                e.printStackTrace();
                pool = new Pool();
            }

        } else {
            pool = new Pool();
        }

        savePool();
    }

    private static void saveConfig() {
        try {
            var configFile = new File("config/wondertrade/main.json");
            var fileWriter = new FileWriter(configFile);
            BaseConfig.GSON.toJson(config, fileWriter);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            logger.error("Failed to save the config!");
            e.printStackTrace();
        }
    }

    public static void savePool() {
        try {
            var configFile = new File("config/wondertrade/pool.json");
            var fileWriter = new FileWriter(configFile);
            BaseConfig.GSON.toJson(pool, fileWriter);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            logger.error("Failed to save the wondertrade pool!");
            e.printStackTrace();
        }
    }


    private static final class WorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        private static final AtomicInteger COUNT = new AtomicInteger(0);

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            thread.setDaemon(true);
            thread.setName("WonderTrade Worker - " + COUNT.getAndIncrement());
            thread.setContextClassLoader(WonderTrade.class.getClassLoader());
            return thread;
        }
    }

    private static final class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            logger.error("Thread " + t.getName() + " threw an uncaught exception");
            e.printStackTrace();
        }
    }
}
