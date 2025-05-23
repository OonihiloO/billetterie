@import org.montrealjug.billetterie.entity.Activity
@import org.montrealjug.billetterie.entity.Booker
@import org.montrealjug.billetterie.ui.PresentationEvent
@import org.montrealjug.billetterie.ui.Utils

@param Booker booker
@param PresentationEvent event
@param boolean fullRefresh = false

<div id="event-activities" hx-swap-oob="true">
@if (booker != null)
  <form id="booker-form"
        hx-post="/bookers/${booker.getEmailSignature()}"
        hx-on:htmx:config-request="if (event.detail.triggeringEvent.submitter.id === 'booker-registration-cancel') { event.detail.parameters['reset'] = 'true'; }">
    !{var bookerParticipants = event.bookedParticipants(booker);}
    @for (var activityParticipants : bookerParticipants.entrySet())
        @for (var participant : activityParticipants.getValue())
          <input type="hidden" name="activity-participant" value="${activityParticipants.getKey()}-${participant}" />
        @endfor
    @endfor
    @if (bookerParticipants.isEmpty())
      <input type="hidden" name="dummy" value="dummy" />
    @endif
  </form>
@endif
  <!-- Activities Table -->
  <h3 class="text-md font-semibold text-gray-700 md:ml-0">Activities</h3>
  <div class="bg-white rounded md:rounded-md md:shadow">
    @for (var activity: event.activities())
      !{ var registrationStatus = activity.registrationStatus(); }
      <article id="activity-${activity.id()}" class="px-4 py-2 flex flex-row items-start justify-between flex-wrap md:py-4">
        <div class="size-12 shrink-0 mr-2 md:size-16 md:mr-4">
          @template.layouts.image_with_fallback(
          entityType = "activity",
          cssClass = "object-cover object-center size-full rounded-lg",
          id = activity.id()
          )
        </div>
        <div class="grow-1 flex flex-col justify-between">
          <h4>${activity.title()}</h4>
          <details name="activity" class="mt-2">
            <summary class="italic text-gray-700 list-none cursor-pointer">details</summary>
            <div class="mt-2">$unsafe{activity.description()}</div>
          </details>
        </div>
        <div class="flex flex-col justify-between items-end mx-2 self-stretch md:mx-4">
          <time>${Utils.TIME_FORMAT.format(activity.time())}</time>
          <div class="flex flex-row items-center">
            @if (registrationStatus == Activity.RegistrationStatus.OPEN)
              <span class="text-left text-sm text-gray-700">seats:</span><span class="ml-2 w-12 text-right">${activity.participants().size()}/${activity.maxParticipants()}</span>
            @else
              <span class="text-left text-sm text-gray-700">waiting list:</span><span class="ml-2 w-12 text-right">${activity.waitingParticipants().size()}/${activity.maxWaitingQueue()}</span>
            @endif
          </div>
        </div>
        @if (booker != null)
        <div class="p-2 basis-full" id="activity-${activity.id()}-participants">
          !{var participants = activity.participants(booker);}
          @if (!participants.isEmpty())
            <h3 class="pt-2">Registered participants</h3>
            @for (var participant : participants)
              <div class="py-2">
                <span>${participant.firstName()} ${participant.lastName()} (${participant.age()} year old)</span>
              </div>
            @endfor
          @endif
          !{var waitingParticipants = activity.waitingParticipants(booker);}
          @if (!waitingParticipants.isEmpty())
            <h3 class="pt-2">Registered on waiting list</h3>
            @for (var participant : waitingParticipants)
              <div class="py-2">
                <span>${participant.firstName()} ${participant.lastName()} (${participant.age()} year old)</span>
              </div>
            @endfor
          @endif
          <div class="mt-2" id="activity-${activity.id()}-action-button">
            <form hx-post="/bookers/${booker.getEmailSignature()}/${activity.id()}" hx-target="#modal">
              <input type="hidden" name="edit" value="true" />
              @for (var participant : activity.allParticipants(booker))
                <input type="hidden" name="activity-participant" value="${activity.id()}-${participant.id()}" />
              @endfor
              @for (var bookedParticipant : event.alreadyBookedParticipants(booker, activity))
                <input type="hidden" name="booked-participant" value="${bookedParticipant.formValue()}" />
              @endfor
              @if (registrationStatus != Activity.RegistrationStatus.CLOSED || !participants.isEmpty() || !waitingParticipants.isEmpty())
                <button type="submit"
                        class="bg-white text-d4k-orange px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300"
                >Manage Participants</button>
              @else
                <button type="button"
                        class="bg-gray-100 text-d4k-orange opacity-75 px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300"
                        disabled
                >Activity full, booking closed</button>
              @endif
            </form>
          </div>
        </div>
        @endif
      </article>
    @endfor
  </div>
</div>

@if (fullRefresh)
<div id="booker-registration-button" class="flex justify-end gap-4 pt-2 pr-2 md:pr-0 md:pt-4 lg:pt-8" hx-swap-oob="true">
  <button form="booker-form"
          type="submit"
          class="bg-gray-100 text-d4k-orange opacity-75 px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300" disabled
  >Cancel</button>
  <button form="booker-form"
          type="submit"
          class="bg-gray-400 text-white px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-gray-300" disabled
  >Update Registration</button>
</div>
@endif
