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

        stateMachine.currentState = 'state_a'
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

        stateMachine.currentState = 'state_a'
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

        stateMachine.currentState = 'state_a'
        stateMachine.transition('event_a')

        assert stateMachine.currentState == 'state_b'
        assert actionPerformed
    }

    @Test
    void testDslBuildsCorrectStructure()
    {
        Closure cl = { println "cl" }
        Closure cl1 = { println "cl1" }
        Closure cl2 = { println "cl2" }
        Closure cl3 = { println "cl3" }
        def expected_state_machine = [
            (null): [
                [event: "customerSubmitsRegistrationForVerifificationByCdn", to: "WAITING_FOR_CDN_VERIFICATION", action: cl],
                [event: "customerSelectsToBeNotifiedWhenNmiReady", to: "WAITING_FOR_NMI_TO_BE_READY"]
            ],
            ("WAITING_FOR_CDN_VERIFICATION"): [
                [event: "verifiedByCdn", to: "ACTIVE", action: cl1],
                [event: "retailerApprovesRegistration", to: "ACTIVE", action: cl1],
                [event: "retailerRejectsRegistration", to: "REJECTED_BY_RETAILER"],
                [event: "retailerDetectsRegistrationError", to: "CORRECTING_REGISTRATION"],
                [event: "esbReportedCustomerMovedOut", to: "MOVED_OUT", action: cl2]
            ],
            ("WAITING_FOR_VERIFICATION"): [
                [event: "retailerApprovesRegistration", to: "ACTIVE", action: cl1],
                [event: "retailerRejectsRegistration", to: "REJECTED_BY_RETAILER"],
                [event: "retailerDetectsRegistrationError", to: "CORRECTING_REGISTRATION"],
                [event: "esbReportedCustomerMovedOut", to: "MOVED_OUT", action: cl2],
                [event: "customerChangesRetailer", to: "CHANGED_RETAILER", action: cl3]
            ]
        ]

        def actualStateMachine = StateMachineBuilder.build {
            state(null) {
                event("customerSubmitsRegistrationForVerifificationByCdn") {
                    transitionTo "WAITING_FOR_CDN_VERIFICATION"
                    action(cl)
                }
                event("customerSelectsToBeNotifiedWhenNmiReady", to: "WAITING_FOR_NMI_TO_BE_READY")
            }

            state("WAITING_FOR_CDN_VERIFICATION") {
                event "verifiedByCdn", to: "ACTIVE", action: cl1
                event "retailerApprovesRegistration", to: "ACTIVE", action: cl1
                event "retailerRejectsRegistration", to: "REJECTED_BY_RETAILER"
                event "retailerDetectsRegistrationError", to: "CORRECTING_REGISTRATION"
                event "esbReportedCustomerMovedOut", to: "MOVED_OUT", action: cl2
            }

            state("WAITING_FOR_VERIFICATION") {
                onRetailerApprovesRegistration to: "ACTIVE", action: cl1
                onRetailerRejectsRegistration to: "REJECTED_BY_RETAILER"
                onRetailerDetectsRegistrationError to: "CORRECTING_REGISTRATION"
                onEsbReportedCustomerMovedOut(to: "MOVED_OUT") {
                    action cl2
                }
                onCustomerChangesRetailer {
                    to "CHANGED_RETAILER"
                    action cl3
                }
            }
        }

        assert actualStateMachine.stateMachineMap == expected_state_machine
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

        stateMachine.currentState = 'state_a'
        stateMachine.transition("event_a")

        assert "state_c" == stateMachine.currentState
    }
}
