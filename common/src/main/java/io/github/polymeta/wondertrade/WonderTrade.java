package io.github.polymeta.wondertrade;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import io.github.polymeta.wondertrade.commands.RegeneratePool;
import io.github.polymeta.wondertrade.commands.Reload;
import io.github.polymeta.wondertrade.commands.Trade;
import io.github.polymeta.wondertrade.configuration.BaseConfig;
import io.github.polymeta.wondertrade.configuration.Pool;
import kotlin.Unit;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
    public static final MiniMessage miniMessage = MiniMessage.miniMessage();

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
            Reload.register(dispatcher);
        });
        LifecycleEvent.SERVER_STARTED.register((instance) -> {
            if(WonderTrade.pool.pokemon.isEmpty()) {
                logger.info("Regenerating pool as it is empty");
                WonderTrade.regeneratePool(WonderTrade.config.poolSize);
            }
        });
        //TODO register message ticks
    }

    public static void regeneratePool(int size) {
        var randomProp = PokemonProperties.Companion.parse("species=random", " ", "=");
        var blacklist = config.blacklist.stream().map(s -> PokemonProperties.Companion.parse(s, " ", "=")).toList();
        pool.pokemon.clear();
        for (int i = 0; i < size; i++) {
            randomProp.setLevel(rng.nextInt(Math.max(1, config.poolMinLevel), Math.min(Cobblemon.config.getMaxPokemonLevel(), config.poolMaxLevel)));
            Pokemon pokemon;
            while(true) {
                pokemon = randomProp.create();
                final Pokemon finalPokemon = pokemon;
                if(blacklist.stream().noneMatch(prop -> prop.matches(finalPokemon))) {
                    break;
                }
            }
            pool.pokemon.add(pokemon.createPokemonProperties(PokemonPropertyExtractor.ALL).asString(" "));
        }
        savePool();
    }

    public static void loadConfig() {
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
        if(config.poolMinLevel > config.poolMaxLevel) {
            logger.warn("Pool min level can not be bigger than max level, adjusting range to 1-CobbleMaxLevel...");
            config.poolMinLevel = 1;
            config.poolMaxLevel = Cobblemon.config.getMaxPokemonLevel();
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
