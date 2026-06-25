## Context

The setup screen currently renders `EnableNotificationsPrompt` only when `alarmNotificationsAvailable` is false. That gives the correct initial state, but changes while the screen is already composed appear abruptly. This is visible on newer Android versions where the notification permission dialog can be shown immediately after app start and its result updates the screen underneath.

## Goals / Non-Goals

**Goals:**

- Preserve immediate correctness on first composition: if notifications are unavailable before the setup screen is shown, the prompt is visible without waiting for an animation to finish.
- Animate only subsequent visible-state changes while the setup screen remains composed.
- Keep the prompt in the same bottom-end position and keep the existing settings click behavior.
- Keep the implementation local to setup-screen UI state and tests.

**Non-Goals:**

- Change notification availability detection.
- Change the prompt text, placement, color, or settings destination.
- Add a general-purpose animation framework.
- Animate the whole setup screen or duration grid.

## Decisions

- Use Compose visibility animation for the prompt rather than manually animating alpha on a permanently composed `Text`.
  - Rationale: visibility animation keeps semantics aligned with the visible state after transitions and avoids keeping an invisible clickable target around.
  - Alternative considered: `Modifier.alpha` with conditional click handling. That is more error-prone because invisible content can remain in semantics or hit testing if not carefully gated.

- Track whether the prompt has completed its initial composition before enabling animation.
  - Rationale: the initial UI must reflect the current notification state immediately. A first-render fade-in would make the warning temporarily absent or partially visible even though the state is already known.
  - Alternative considered: always use `AnimatedVisibility`. This is simpler but conflicts with the initial-state requirement.

- Use short Material-style fade timings and no movement animation.
  - Rationale: the prompt is a small text affordance at the screen edge. A fade is enough to communicate state change without shifting layout or distracting from the duration choices.
  - Alternative considered: fade plus slide. This adds visual motion near navigation/system bars without a functional benefit.

## Risks / Trade-offs

- Compose animation tests can become timing-sensitive. -> Use `mainClock` control in Robolectric Compose tests and assert initial and settled states.
- During fade-out, the prompt may remain visible briefly after notifications are enabled. -> This is intentional transition behavior; click handling should remain tied to visible content during the transition and disappear when the animation completes.
- If the notification permission result arrives before the first setup composition completes, it may be treated as initial state. -> This is acceptable because the requirement is about changes while the app screen is already visible.
