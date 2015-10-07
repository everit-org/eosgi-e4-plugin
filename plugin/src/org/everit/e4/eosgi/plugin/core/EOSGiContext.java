package org.everit.e4.eosgi.plugin.core;

import java.util.List;
import java.util.Observer;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.everit.e4.eosgi.plugin.core.dist.DistRunner;

public interface EOSGiContext extends IMavenProjectChangedListener {

  void delegateObserver(Observer observer);

  void dispose();

  List<String> environmentNames();

  void forcedStop(String environmentName);

  void generate(String environmentName, IProgressMonitor monitor);

  void refresh(ContextChange contextChange);

  void removeObserver(Observer observer);

  Optional<DistRunner> runner(String environment);
}
