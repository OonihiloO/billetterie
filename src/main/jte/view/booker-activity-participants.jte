@import org.montrealjug.billetterie.entity.Booker
@import org.montrealjug.billetterie.ui.BookerController.ParticipantFormType
@import org.montrealjug.billetterie.ui.PresentationActivity
@import org.montrealjug.billetterie.ui.PresentationEvent

@param PresentationEvent event
@param PresentationActivity activity
@param Booker booker
@param boolean willLoseSpot
@param boolean onChange
<div id="event-activities-modal" class="fixed inset-0 flex items-center gap-2 justify-center bg-black/50 z-50" hx-swap-oob="${onChange}">
  <div class="bg-white rounded-lg shadow-md p-6 w-96">
    <h2 class="text-lg font-semibold text-gray-800 mb-4">Participants for ${activity.title()}</h2>
    @if (willLoseSpot)
      <span class="inline-block mt-2 text-red-400 p-2 bg-red-50 rounded border-1 border-red-400">
        This activity has a waiting list, if you remove a registered participant, you won't have a guaranteed spot.
      </span>
    @endif
    <div class="mt-2">
      <form hx-post="/bookers/${booker.getEmailSignature()}/participants"
            hx-target="#modal">
        @for (var bookedParticipant : event.alreadyBookedParticipants(booker, activity))
          <input type="hidden" name="booked-participant" value="${bookedParticipant.formValue()}" />
        @endfor
        @for (var participant : activity.allParticipants(booker))
          <input type="hidden" name="activity-participant" value="${activity.id()}-${participant.id()}" />
        @endfor
        <input type="hidden" name="activityId" value="${activity.id()}" />
        <input type="hidden" name="action" value="${ParticipantFormType.START}" />
        <button type="submit"
                class="bg-white text-d4k-orange px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300"
        >Create Participant</button>
      </form>
    </div>
    <form id="activity-${activity.id()}-participants-form"
        hx-post="/bookers/${booker.getEmailSignature()}/${activity.id()}"
        hx-on:htmx:after-request="if (event.detail.requestConfig.triggeringEvent.type !== 'change') { billetterie.closeModal(); }"
        hx-trigger="change, submit"
        hx-on:htmx:config-request="if (event.detail.triggeringEvent.type === 'change') { event.detail.parameters['on-change'] = 'true'; }"
        class="mt-2"
    >
      !{ var availableSeats = activity.availableSeats(booker); }
      <input type="hidden" id="activity-${activity.id()}-available-seats" value="${availableSeats}" />
      @for (var bookedParticipant : event.alreadyBookedParticipants(booker, activity))
        <input type="hidden" name="booked-participant" value="${bookedParticipant.formValue()}" />
      @endfor
      <input type="hidden" value="false" name="edit" />
    !{var participants = activity.participants(booker);}
    @if (!participants.isEmpty())
      <h3 class="pt-2">Registered participants</h3>
      @for (var participant : participants)
        <div class="py-2 flex items-center gap-2">
            <input id="participant-${participant.id()}"
                   type="checkbox"
                   name="activity-participant"
                   value="${activity.id()}-${participant.id()}"
                   class="w-4 h-4 accent-d4k-orange bg-gray-100 border-gray-300 rounded-sm focus:ring-d4k-orange/150"
                   checked>
            <label for="participant-${participant.id()}">${participant.firstName()} ${participant.lastName()} (${participant.age()} year old)</label>
        </div>
      @endfor
    @endif
    !{var waitingParticipants = activity.waitingParticipants(booker);}
    @if (!waitingParticipants.isEmpty())
      <h3 class="pt-2">Participants on waiting list:</h3>
      @for (var participant : waitingParticipants)
        <div class="py-2 flex items-center gap-2">
            <input id="participant-${participant.id()}"
                   type="checkbox"
                   name="activity-participant"
                   value="${activity.id()}-${participant.id()}"
                   class="w-4 h-4 accent-d4k-orange bg-gray-100 border-gray-300 rounded-sm focus:ring-d4k-orange/150"
                   checked>
            <label for="participant-${participant.id()}">${participant.firstName()} ${participant.lastName()} (${participant.age()} year old)</label>
        </div>
      @endfor
    @endif
    !{var nonBookedParticipants = event.nonBookedParticipants(booker);}
    @if (!nonBookedParticipants.isEmpty())
      <h3 class="pt-2">Available participants:</h3>
      @if (availableSeats > (participants.size()) + waitingParticipants.size())
        @for (var participant : nonBookedParticipants)
          <div class="py-2 flex items-center gap-2">
            <input id="participant-${participant.getId()}"
                   type="checkbox"
                   name="activity-participant"
                   value="${activity.id()}-${participant.getId()}"
                   class="w-4 h-4 accent-d4k-orange bg-gray-100 border-gray-300 rounded-sm focus:ring-d4k-orange/150">
            <label for="participant-${participant.getId()}">${participant.getFirstName()} ${participant.getLastName()} (${participant.getAge()} year old)</label>
          </div>
        @endfor
      @else
        @for (var participant : nonBookedParticipants)
          <div class="py-2 flex items-center gap-2">
            <input id="participant-${participant.getId()}"
                   type="checkbox"
                   name="activity-participant"
                   value="${activity.id()}-${participant.getId()}"
                   class="w-4 h-4 accent-d4k-orange bg-gray-100 border-gray-300 rounded-sm focus:ring-d4k-orange/150"
                   disabled>
            <label for="participant-${participant.getId()}">${participant.getFirstName()} ${participant.getLastName()} (${participant.getAge()} year old)</label>
          </div>
        @endfor
        <span id="activity-${activity.id()}-full-msg" class="inline-block text-red-400 p-2 bg-red-50 rounded border-red-200 border-1">Activity full, you can't add more participants</span>
      @endif
    @endif
    !{var alreadyBooked = event.alreadyBookedParticipants(booker, activity);}
    @if (!alreadyBooked.isEmpty())
      <h3 class="pt-2">Booked in other activities:</h3>
      @for (var participant : alreadyBooked)
        <div class="py-2 flex items-center gap-2">
          <input id="participant-${participant.id()}"
                 type="checkbox"
                 value="${participant.formValue()}"
                 class="w-4 h-4 accent-d4k-orange/50 bg-gray-100 border-gray-300 rounded-sm focus:ring-d4k-orange/150"
                 checked disabled>
          <label for="participant-${participant.id()}">${participant.firstName()} ${participant.lastName()} (${participant.age()} year old)</label>
        </div>
      @endfor
    @endif
    </form>
    <div class="mt-2 flex gap-2" id="activity-${activity.id()}-action-button">
      <button type="submit"
              form="activity-${activity.id()}-participants-form"
              class="bg-d4k-orange text-white px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300"
      >Update Participants</button>
      <button type="button"
              onclick="billetterie.closeModal();"
              form="activity-${activity.id()}-participants-form"
              class="bg-white text-d4k-orange px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300"
      >Cancel</button>
    </div>
  </div>
</div>

@if (onChange)
<div id="booker-registration-button" class="flex justify-end gap-4 pt-2 pr-2 md:pr-0 md:pt-4 lg:pt-8" hx-swap-oob="true">
  <button form="booker-form"
          type="submit"
          id="booker-registration-cancel"
          class="bg-white text-d4k-orange px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300"
  >Cancel</button>
  <button form="booker-form"
          type="submit"
          class="bg-d4k-orange text-white px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300"
  >Update Registration</button>
</div>
@endif
