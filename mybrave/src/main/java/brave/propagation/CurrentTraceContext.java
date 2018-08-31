package brave.propagation;

import brave.internal.Nullable;
import brave.internal.WrappingExecutorService;
import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public abstract class CurrentTraceContext {
    public CurrentTraceContext() {
    }

    @Nullable
    public abstract TraceContext get();

    public abstract CurrentTraceContext.Scope newScope(@Nullable TraceContext var1);

    public <C> Callable<C> wrap(final Callable<C> task) {
        final TraceContext invocationContext = this.get();

        class CurrentTraceContextCallable implements Callable<C> {
            CurrentTraceContextCallable() {
            }

            public C call() throws Exception {
                CurrentTraceContext.Scope scope = CurrentTraceContext.this.newScope(invocationContext);
                Throwable var2 = null;

                //Object var3;
                C var3 = null;
                try {
                    var3 = task.call();
                } catch (Throwable var12) {
                    var2 = var12;
                    //throw var12;
                } finally {
                    if (scope != null) {
                        if (var2 != null) {
                            try {
                                scope.close();
                            } catch (Throwable var11) {
                                ;
                            }
                        } else {
                            scope.close();
                        }
                    }

                }

                return var3;
            }
        }

        return new CurrentTraceContextCallable();
    }

    public Runnable wrap(final Runnable task) {
        final TraceContext invocationContext = this.get();

        class CurrentTraceContextRunnable implements Runnable {
            CurrentTraceContextRunnable() {
            }

            public void run() {
                CurrentTraceContext.Scope scope = CurrentTraceContext.this.newScope(invocationContext);
                Throwable var2 = null;

                try {
                    task.run();
                } catch (Throwable var11) {
                    var2 = var11;
                    //throw var11;
                } finally {
                    if (scope != null) {
                        if (var2 != null) {
                            try {
                                scope.close();
                            } catch (Throwable var10) {
                                ;
                            }
                        } else {
                            scope.close();
                        }
                    }

                }

            }
        }

        return new CurrentTraceContextRunnable();
    }

    public Executor executor(final Executor delegate) {
        class CurrentTraceContextExecutor implements Executor {
            CurrentTraceContextExecutor() {
            }

            public void execute(Runnable task) {
                delegate.execute(CurrentTraceContext.this.wrap(task));
            }
        }

        return new CurrentTraceContextExecutor();
    }

    public ExecutorService executorService(final ExecutorService delegate) {
        class CurrentTraceContextExecutorService extends WrappingExecutorService {
            CurrentTraceContextExecutorService() {
            }

            protected ExecutorService delegate() {
                return delegate;
            }

            protected <C> Callable<C> wrap(Callable<C> task) {
                return CurrentTraceContext.this.wrap(task);
            }

            protected Runnable wrap(Runnable task) {
                return CurrentTraceContext.this.wrap(task);
            }
        }

        return new CurrentTraceContextExecutorService();
    }

    public static final class Default extends CurrentTraceContext {
        static final ThreadLocal<TraceContext> DEFAULT = new ThreadLocal();
        static final InheritableThreadLocal<TraceContext> INHERITABLE = new InheritableThreadLocal();
        final ThreadLocal<TraceContext> local;

        /** @deprecated */
        @Deprecated
        public Default() {
            this(INHERITABLE);
        }

        public static CurrentTraceContext create() {
            return new CurrentTraceContext.Default(DEFAULT);
        }

        public static CurrentTraceContext inheritable() {
            return new CurrentTraceContext.Default(INHERITABLE);
        }

        Default(ThreadLocal<TraceContext> local) {
            if (local == null) {
                throw new NullPointerException("local == null");
            } else {
                this.local = local;
            }
        }

        public TraceContext get() {
            return (TraceContext)this.local.get();
        }

        public CurrentTraceContext.Scope newScope(@Nullable TraceContext currentSpan) {
            final TraceContext previous = (TraceContext)this.local.get();
            this.local.set(currentSpan);

            class DefaultCurrentTraceContextScope implements CurrentTraceContext.Scope {
                DefaultCurrentTraceContextScope() {
                }

                public void close() {
                    Default.this.local.set(previous);
                }
            }

            return new DefaultCurrentTraceContextScope();
        }
    }

    public interface Scope extends Closeable {
        void close();
    }
}
