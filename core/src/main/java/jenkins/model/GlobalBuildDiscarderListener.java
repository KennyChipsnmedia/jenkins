/*
 * The MIT License
 *
 * Copyright 2019 Daniel Beck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.model;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.util.LogTaskListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Run background build discarders on an individual job once a build is finalized
 */
@Extension
@Restricted(NoExternalUse.class)
public class GlobalBuildDiscarderListener extends RunListener<Run> {

    private static final Logger LOGGER = Logger.getLogger(GlobalBuildDiscarderListener.class.getName());

    // Kenny
    private static final Map<Integer, Run> runners = new ConcurrentHashMap<>();

    @Override
    public void onStarted(Run run, TaskListener listener) {
        if (run != null) {
            runners.put(run.getNumber(), run);
        }
    }

    /**
     * Author: Kenny
     */
    @Override
    public void onFinalized(Run run) {
        runners.remove(run.getNumber());

//        LOGGER.log(Level.FINE, "Kenny check if parent job name:{0} exists", new Object[] {run.getParent().getName()});
        LOGGER.log(Level.FINER, "Kenny GlobalBuildDiscarderListener check if parent job name:{0} exists", new Object[] {run.getParent().getName()});

        List<Map.Entry<Integer, Run>> siblings = runners.entrySet().stream().filter(r -> r.getValue().getParent().getName().equals(run.getParent().getName())).collect(Collectors.toList());
        if (siblings.size() == 0) {
            Job job = run.getParent();
            BackgroundGlobalBuildDiscarder.processJob(new LogTaskListener(LOGGER, Level.FINE), job);
        }

        /* Jenkins original
        Job job = run.getParent();
        BackgroundGlobalBuildDiscarder.processJob(new LogTaskListener(LOGGER, Level.FINE), job);
        */
    }
}
