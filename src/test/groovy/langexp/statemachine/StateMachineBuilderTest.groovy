package langexp.statemachine

import org.junit.Test

class StateMachineBuilderTest
{
    @Test
    void testBasicTransition()
    {
        def stateMachine = StateMachineBuilder.build {
            state('state_a') {
                event('event_a', to: 'state_b')
            }
        }

        stateMachine.start('state_a')
        stateMachine.transition('event_a')

        assert stateMachine.currentState == 'state_b'
    }

    @Test
    void testGuardedTransition()
    {
        def stateMachine = StateMachineBuilder.build {
            state('state_a') {
                event('event_a', to: 'state_b', guard: { false })
                event('event_a', to: 'state_c', guard: { true })
            }
        }

        stateMachine.start('state_a')
        stateMachine.transition('event_a')

        assert stateMachine.currentState == 'state_c'
    }

    @Test
    void testActionPerformed()
    {
        boolean actionPerformed = false
        def stateMachine = StateMachineBuilder.build {
            state('state_a') {
                event('event_a', to: 'state_b', action: { actionPerformed = true })
            }
        }

        stateMachine.start('state_a')
        stateMachine.transition('event_a')

        assert stateMachine.currentState == 'state_b'
        assert actionPerformed
    }

    @Test
    void shouldAllowAnActionOnEntryToAState()
    {
        def trans = []
        def stateMachine = StateMachineBuilder.build {
            state(null) {
                onEvent1 to: 'A', action: { trans << 1 }
            }
            state('A') {
                onEntry { trans << 2 }
                onEvent2 to: 'B', action: { trans << 3 }
            }
            state('B') {
                onEntry { trans << 4 }
                onEvent1 to: 'A', action: { trans << 5 }
            }
        }

        stateMachine.transition('event1')
        stateMachine.transition('event2')
        stateMachine.transition('event1')

        assert trans == [1, 2, 3, 4, 5, 2]
    }

    @Test
    void testGuardedTransitionWithDsl()
    {
        def stateMachine = StateMachineBuilder.build {
            state("state_a") {
                event "event_a", guard: { false }, to: "state_b"
                onEvent_a {
                    guard { true }
                    to "state_c"
                }
            }
            state "state_b"
            state "state_c"
        }

        stateMachine.start('state_a')
        stateMachine.transition("event_a")

        assert "state_c" == stateMachine.currentState
    }

    @Test
    void testGlobalActionPerformed()
    {
        boolean actionPerformed = false
        def stateMachine = StateMachineBuilder.build {
            state('state_a') {
                event('event_a', to: 'state_b')
            }
            action { actionPerformed = true }
        }

        stateMachine.start('state_a')
        stateMachine.transition('event_a')

        assert stateMachine.currentState == 'state_b'
        assert actionPerformed
    }

    @Test
    void testTerminationActionPerformed()
    {
        boolean actionPerformed = false
        def stateMachine = StateMachineBuilder.build {
            state('state_a') {
                event('event_a', to: 'state_b')
            }
            finalState('state_b')
            onTermination { actionPerformed = true }
        }

        stateMachine.start('state_a')
        stateMachine.transition('event_a')

        assert actionPerformed
        assert stateMachine.currentState == 'state_b'
        assert stateMachine.terminated
    }

    @Test
    void testGlobalEvent()
    {
        boolean actionPerformed = false
        def stateMachine = StateMachineBuilder.build {
            state('state_a')
            event('event_a', to: 'state_b', action: { actionPerformed = true })
        }

        stateMachine.start('state_a')
        stateMachine.transition('event_a')

        assert stateMachine.currentState == 'state_b'
        assert actionPerformed
    }
}
