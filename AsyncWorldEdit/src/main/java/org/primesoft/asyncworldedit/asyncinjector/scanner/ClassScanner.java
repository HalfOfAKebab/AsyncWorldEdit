/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit.asyncinjector.scanner;

import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.worldedit.extent.world.WatchdogTickingExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.ClipboardPattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.registry.BlockRegistry;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.classScanner.IClassFilter;
import org.primesoft.asyncworldedit.api.inner.IClassScanner;
import org.primesoft.asyncworldedit.api.inner.IClassScannerResult;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.configuration.DebugLevel;
import org.primesoft.asyncworldedit.configuration.PermissionGroup;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.Pair;
import org.primesoft.asyncworldedit.worldedit.extent.inventory.FixedBlockBagExtent;

/**
 * The class scanner
 *
 * @author SBPrime
 */
public abstract class ClassScanner implements IClassScanner {
    private static final Object ITEM = new Object();
    
    /**
     * List of all filters
     */
    private final Map<IClassFilter, Object> m_filters = new ConcurrentHashMap<>();

    private final ConfigurableClassFilter m_configurableFilter = new ConfigurableClassFilter();
    
    private IClassScannerEntry[] m_blackList = new IClassScannerEntry[0];
    
    /**
     * Is the class scanner initialized
     */
    private boolean m_isInitialized = false;

    /**
     * Initialize the class scanner
     * @return 
     */
    @Override
    public IClassScanner initialize() {
        m_blackList = getBlackList();
        m_isInitialized = true;
        return this;
    }

    /**
     * Scan object (and all fields) for T
     *
     * @param types The types of classes to find
     * @param o Object to find
     * @return
     */
    @Override
    public List<IClassScannerResult> scan(
            final Class<?>[] types,
            final Object o) {

        if (!m_isInitialized) {
            throw new IllegalStateException("Class scanner not initialized");
        }
        
        final List<IClassScannerResult> result = new ArrayList<>();
        if (o == null) {
            return result;
        }

        final Queue<ScannerQueueEntry> toScan = new ArrayDeque<>();
        final HashSet<Object> scanned = new HashSet<>();

        boolean debugOn = ConfigProvider.messages().debugLevel().isAtLeast(DebugLevel.DEBUG);
        toScan.add(new ScannerQueueEntry(o, null, null));

        if (debugOn) {
            log("****************************************************************");
            log("* Scanning classes");
            log("****************************************************************");
        }

        /**
         * We do not need to check if first object (o) is of type "type" because
         * it will by impossible to inject it anyways.
         */
        while (!toScan.isEmpty()) {
            final ScannerQueueEntry entry = toScan.poll();

            final Object cObject = entry.getValue();
            final Class<?> cClass = entry.getValueClass();
            if (cObject == null || cClass == null) {
                continue;
            }

            String sParent;
            if (debugOn) {
                sParent = String.format("%1$s:%2$s", Integer.toHexString(cObject.hashCode()), cObject.getClass().getName());
            } else {
                sParent = null;
            }

            if (scanned.contains(cObject)) {
                if (debugOn) {
                    log(String.format("* Skip:\t%1$s", sParent));
                }
            } else {
                if (debugOn) {
                    log(String.format("* Scanning:\t%1$s", sParent));
                }

                int added = scanProcess(types, result, toScan, debugOn, entry, cObject, cClass);
                scanned.add(cObject);

                if (debugOn) {
                    log(String.format("* Added:\t%1$s objects.", added));
                }
            }
        }

        if (debugOn) {
            log("****************************************************************");
        }
        return result;
    }

    public int scanProcess(
            final Class<?>[] types,
            final List<IClassScannerResult> result,
            final Queue<ScannerQueueEntry> toScan,
            final boolean debugOn,

            final ScannerQueueEntry entry,
            final Object curentObject,
            final Class<?> curentClass) {

        int added = 0;

        final List<Pair<Throwable, String>> errors = new LinkedList<>();
        final InOutParam<Throwable> generalError = InOutParam.Out();

        try {
            for (ScannerQueueEntry f : unpack(curentClass, curentObject, errors)) {
                final Object t = f.getValue();
                final Class<?> ct = f.getValueClass();

                if (t != null && ct != null) {
                    final String classMsg = debugOn ? getDebugMessage(f) : null;

                    for (Class<?> type : types) {
                        if (type.isAssignableFrom(ct)) {
                            if (classMsg != null) {
                                log(String.format("* F %1$s", classMsg));
                            }

                            result.add(new ClassScannerResult(t, t.getClass(), f.getParent(), f.getField()));
                            break;
                        }
                    }

                    if (isPrimitive(ct) ||
                        isBlackList(ct) ||
                        isStatic(f.getField()) ||
                        isBlackList(curentClass, f.getField()) ||
                        Objects.equals(t, entry.getParent()))
                    {
                        if (classMsg != null) {
                            log(String.format("* - %1$s", classMsg));
                        }
                    } else {
                        toScan.add(f);
                        added++;

                        if (classMsg != null) {
                            log(String.format("* + %1$s", classMsg));
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            generalError.setValue(ex);
        }

        if (generalError.isSet() || !errors.isEmpty()) {
            log("-----------------------------------------------------------------------");
            log("Warning: Class scanner encountered an error while scanning class");

            if (generalError.isSet()) {
                final Throwable ex = generalError.getValue();
                log(String.format("General exception: %1$s, %2$s", ex.getClass().getName(), ex.getMessage()));
                ExceptionHelper.printStack(ex, "");
            }
            if (!errors.isEmpty()) {
                log("Errors:");
                errors.forEach(e -> log(" - " + e.getX2() + ": " +
                        e.getX1().getClass().getName() + ", " +
                        e.getX1().getMessage()));
            }
            log(String.format("Class: %1$s", curentClass));
            log(String.format("Object: %1$s", curentObject));
            log("Send this message to the author of the plugin!");
            log("https://github.com/SBPrime/AsyncWorldEdit/issues");
            log("-----------------------------------------------------------------------");
        }

        return added;
    }

    public String getDebugMessage(final ScannerQueueEntry f) {
        String classMsg;
        final Field field = f.getField();
        final String sValue = String.format("%1$s:%2$s", Integer.toHexString(f.getValue().hashCode()), f.getValueClass().getName());
        final String sField = field != null ? field.getName() : "?";

        classMsg = String.format("%s = %s", sField, sValue);
        return classMsg;
    }

    /**
     * Checks if the class is a primitive (number or string)
     *
     * @param oClass
     * @return
     */
    private static boolean isPrimitive(Class<?> oClass) {
        return oClass.isPrimitive()
                || (Character.class.isAssignableFrom(oClass))
                || (Number.class.isAssignableFrom(oClass))
                || (Boolean.class.isAssignableFrom(oClass))
                || (String.class.isAssignableFrom(oClass))
                || (UUID.class.isAssignableFrom(oClass));
    }

    /**
     * Get all fields from a class
     *
     * @param oClass
     * @param o
     * @return
     */
    private Iterable<ScannerQueueEntry> unpack(
            final Class<?> oClass,
            final Object o,
            final List<Pair<Throwable, String>> errors) {

        final HashSet<ScannerQueueEntry> result = new HashSet<>();

        if (isPrimitive(oClass) || isBlackList(oClass)) {
            return result;
        }

        unpackArray(oClass, o, result);
        unpackIterable(oClass, o, result);
        unpackFields(oClass, o, errors, result);

        return result;
    }

    private void unpackArray(
            final Class<?> oClass,
            final Object o,
            final HashSet<ScannerQueueEntry> result) {

        if (!oClass.isArray()) {
            return;
        }

        Class<?> componenClass = oClass;
        while (componenClass.isArray()) {
            componenClass = componenClass.getComponentType();
        }
        if (!isPrimitive(componenClass) && !isBlackList(componenClass)) {
            for (Object t : (Object[]) o) {
                if (t != null) {
                    result.add(new ScannerQueueEntry(t, o, null));
                }
            }
        }
    }

    private void unpackIterable(
            final Class<?> oClass,
            final Object o,
            final HashSet<ScannerQueueEntry> result) {

        if (!Iterable.class.isAssignableFrom(oClass)) {
            return;
        }

        for (Object t : (Iterable<Object>) o) {
            if (t != null) {
                result.add(new ScannerQueueEntry(t, o, null));
            }
        }
    }

    private void unpackFields(
            final Class<?> oClass,
            final Object o,
            final List<Pair<Throwable, String>> errors,
            final HashSet<ScannerQueueEntry> result) {

        for (Field f : getAllFields(oClass)) {
            try {
                boolean restore = !f.isAccessible();
                if (restore) {
                    f.setAccessible(true);
                }

                Object t = f.get(o);
                if (t != null) {
                    result.add(new ScannerQueueEntry(t, o, f));
                }

                if (restore) {
                    f.setAccessible(false);
                }
            } catch (Throwable ex) {
                errors.add(new Pair<>(ex, "Unpack field '" + f.getName() + "'"));
            }
        }
    }

    private boolean isBlackList(Class<?> oClass) {
        return isBlackList(oClass, null);
    }

    /**
     * Get the class scanner black list entries
     * @return 
     */
    protected IClassScannerEntry[] getBlackList() {
        return Stream.of(
            new ClassScannerEntry(WatchdogTickingExtent.class, "watchdog"),
            new FuzyClassScannerEntry("com.sk89q.worldedit.bukkit.adapter.impl.Spigot"),
            new ClassScannerEntry(BlockMask.class, "blocks"),
            new ClassScannerEntry("com.sk89q.worldedit.extent.reorder.ChunkBatchingExtent", "batches"),
            new ClassScannerEntry("org.primesoft.asyncworldedit.blockshub.BlocksHubBridge"),
            new FuzyClassScannerEntry("com.sk89q.worldedit.extent.reorder.MultiStageReorder$"),
            new FuzyClassScannerEntry("com.sk89q.worldedit.extent.reorder.ChunkBatchingExtent$"),
            new ClassScannerEntry(FixedBlockBagExtent.class, "missingBlocks"),
            new ClassScannerEntry(com.sk89q.worldedit.internal.expression.Expression.class),
            new ClassScannerEntry(BlockMap.class),
            new ClassScannerEntry(ChangeSet.class),
            new ClassScannerEntry(EditSession.class),
            new ClassScannerEntry(Region.class),
            new ClassScannerEntry(BlockVector3.class),
            new ClassScannerEntry(World.class),
            new ClassScannerEntry(Change.class),
            new ClassScannerEntry(Vector3.class),
            new ClassScannerEntry(BlockStateHolder.class),
            new ClassScannerEntry(BaseBlock.class),
            new ClassScannerEntry(BlockState.class),
            new ClassScannerEntry(PermissionGroup.class),
            new ClassScannerEntry(IPlayerEntry.class),
            new ClassScannerEntry(Clipboard.class),
            new ClassScannerEntry(BlockRegistry.class),
            new ClassScannerEntry(RandomPattern.class),
            new ClassScannerEntry(ClipboardPattern.class),
            new ClassScannerEntry(BlockPattern.class),
            new ClassScannerEntry(YAMLNode.class),
            new ClassScannerEntry(Field.class),
            new ClassScannerEntry(Method.class),
            new ClassScannerEntry("com.sk89q.wepif.PermissionsResolver"),
            new ClassScannerEntry(Logger.class),
            new ClassScannerEntry(Player.class),
            new ClassScannerEntry(Actor.class),
            new ClassScannerEntry(ChangeSet.class),
            new ClassScannerEntry(Entity.class),
            new FuzyClassScannerEntry("net.minecraft."),
            new FuzyClassScannerEntry("io.netty.")
        ).filter(i -> i.isValid())
         .toArray(IClassScannerEntry[]::new);
    }

    private boolean isBlackList(Class<?> oClass, Field f) {
        for (IClassScannerEntry c : m_blackList) {
            if (c.isMatch(oClass, f)) {
                return true;
            }
        }

        for(IClassFilter filter : m_filters.keySet()) {
            if (!filter.accept(oClass, f)) {
                return true;
            }
        }
        
        if (!m_configurableFilter.accept(oClass, f)) {
            return true;
        }
        
        return false;
    }

    /**
     * Get all fields for class (including supper)
     *
     * @param oClass
     * @return
     */
    private static List<Field> getAllFields(Class<?> oClass) {
        List<Field> result = new ArrayList<>();

        while (oClass != null) {
            result.addAll(Arrays.asList(oClass.getDeclaredFields()));
            oClass = oClass.getSuperclass();
        }
        return result;
    }

    @Override
    public void addFilter(IClassFilter filter) {
        if (filter == null) {
            return;
        }
        
        m_filters.put(filter, ITEM);
    }

    @Override
    public void removeFilter(IClassFilter filter) {
        if (filter == null) {
            return;
        }
        
        m_filters.remove(filter);
    }

    private boolean isStatic(Field f) {
        if (f == null) {
            return false;
        }
        
        return (f.getModifiers() & Modifier.STATIC) == Modifier.STATIC;
    }

    @Override
    public void loadConfig() {
        m_configurableFilter.loadConfig();
    }
}
