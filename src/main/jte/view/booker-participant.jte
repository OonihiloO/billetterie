@import org.montrealjug.billetterie.entity.Booker
@import org.montrealjug.billetterie.ui.BookerController.ParticipantFormType
@import org.montrealjug.billetterie.ui.PresentationActivity
@import org.montrealjug.billetterie.ui.PresentationEvent
@import java.time.LocalDate

@param Booker booker
@param PresentationActivity activity
@param PresentationEvent event
@param boolean backToActivity

<div class="fixed inset-0 flex items-center justify-center bg-black/50 z-50">
  <div class="bg-white rounded-lg shadow-md p-6 w-96">
    <h2 class="text-lg font-semibold text-gray-800 mb-4">Create new Participant</h2>
    <form hx-target="#modal"
          hx-post="/bookers/${booker.getEmailSignature()}/participants"
          id="create-participant-form"
          class="mb-4">
      <fieldset>
        <div class="mb-2">
          <label for="first-name" class="block text-gray-700">First Name</label>
          <input type="text" id="first-name" name="firstName" class="border border-gray-300 rounded px-3 py-2 w-full" required>
        </div>
        <div class="mb-2">
          <label for="last-name" class="block text-gray-700">Last Name</label>
          <input type="text" id="last-name" name="lastName" class="border border-gray-300 rounded px-3 py-2 w-full" required>
        </div>
        <div class="mb-2">
          <label for="yearOfBirth" class="block text-gray-700">Year of Birth</label>
          <input type="number" id="yearOfBirth" name="yearOfBirth" min="${LocalDate.now().getYear() - 19}" max="${LocalDate.now().getYear() - 2}" class="border border-gray-300 rounded px-3 py-2 w-full" required>
        </div>
      </fieldset>
      <input type="hidden" name="activityId" value="${activity.id()}" />
      @for (var participant : activity.allParticipants(booker))
        <input type="hidden" name="activity-participant" value="${activity.id()}-${participant.id()}" />
      @endfor
      @for (var bookedParticipant : event.alreadyBookedParticipants(booker, activity))
        <input type="hidden" name="booked-participant" value="${bookedParticipant.formValue()}" />
      @endfor
      <input id="action" type="hidden" name="action" value="${ParticipantFormType.SAVE}" />
    </form>
    <div class="flex gap-2">
      <button type="submit"
              form="create-participant-form"
              class="bg-d4k-orange text-white px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300"
      >Save Participant</button>
      @if (backToActivity)
        <form  hx-target="#modal" hx-post="/bookers/${booker.getEmailSignature()}/participants">
            <input type="hidden" name="activityId" value="${activity.id()}" />
            @for (var participant : activity.allParticipants(booker))
              <input type="hidden" name="activity-participant" value="${activity.id()}-${participant.id()}" />
            @endfor
            @for (var bookedParticipant : event.alreadyBookedParticipants(booker, activity))
              <input type="hidden" name="booked-participant" value="${bookedParticipant.formValue()}" />
            @endfor
            <input id="action" type="hidden" name="action" value="${ParticipantFormType.CANCEL}" />
            <button type="submit"
                     class="bg-white text-d4k-orange px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300"
            >Cancel</button>
        </form>
      @else
        <button type="submit"
                onclick="billetterie.closeModal();"
                class="bg-white text-d4k-orange px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300"
        >Cancel</button>
      @endif
    </div>
  </div>
</div>
