/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.bukkit.timer;

import com.google.common.collect.Sets;
import io.fairyproject.Fairy;
import io.fairyproject.bukkit.timer.event.TimerClearEvent;
import io.fairyproject.bukkit.timer.impl.PlayerTimer;
import org.bukkit.event.player.PlayerQuitEvent;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.bukkit.metadata.Metadata;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Service(name = "timer")
public class TimerService {

    private Set<Timer> timers;
    private ReentrantLock lock;

    @PostInitialize
    public void onPostInitialize() {
        this.lock = new ReentrantLock();
        this.timers = Sets.newConcurrentHashSet();
        this.startScheduler();

        Events.subscribe(PlayerQuitEvent.class).listen(event -> {
            Metadata.provideForPlayer(event.getPlayer()).ifPresent(PlayerTimer.TIMER_METADATA_KEY, TimerList::clear);
        }).build(FairyBukkitPlatform.PLUGIN);
    }

    protected void add(Timer timer) {
        this.lock.lock();
        this.timers.add(timer);
        this.lock.unlock();
    }

    protected void clear(Timer timer) {
        this.lock.lock();
        this.timers.remove(timer);
        this.lock.unlock();
    }

    public void clearByTimerClass(Class<? extends Timer> timerClass) {
        this.lock.lock();
        this.timers.removeIf(timer -> {
            if (timerClass.isInstance(timer) && timer.clear()) {
                return true;
            }
            return false;
        });
        this.lock.unlock();
    }

    public void startScheduler() {
        Fairy.getTaskScheduler().runRepeated(t -> {
            this.lock.lock();
            Iterator<Timer> iterator = this.timers.iterator();
            while (iterator.hasNext()) {
                Timer timer = iterator.next();
                if (timer.isPaused()) {
                    continue;
                }
                timer.tick();
                if (timer.isElapsed() && timer.elapsed()) {
                    if (!timer.clear(false, TimerClearEvent.Reason.ELAPSED)) {
                        continue;
                    }
                    iterator.remove();
                }
            }
            this.lock.unlock();
        }, 2L);
    }

    public boolean isTimerRunning(Class<? extends Timer> timerClass) {
        return this.getTimer(timerClass) != null;
    }

    public <T extends Timer> T getTimer(Class<T> timerClass) {
        T ret;
        this.lock.lock();
        try {
            ret = timerClass.cast(this.timers
                    .stream()
                    .filter(timerClass::isInstance)
                    .findFirst()
                    .orElse(null));
        } finally {
            this.lock.unlock();
        }
        return ret;
    }
}
