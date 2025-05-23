@param String lastName
@param String firstName
@param String email
@param org.montrealjug.billetterie.ui.BookerController.BookerFormType type
@param String confirmationMessage
@param String errorMsg

<div class="fixed inset-0 flex items-center justify-center bg-black/50 z-50">
  <div class="bg-white rounded-lg shadow-md p-6 w-96">
    <h2 class="text-lg font-semibold text-gray-800 mb-4">Register</h2>
    @if (errorMsg != null)
      <p class="text-red-500 text-sm">${errorMsg}</p>
    @endif
    <form id="booker-booking-form" hx-post="/bookers/registration" hx-target="#booker-booking-form" class="space-y-4">
    @if (confirmationMessage != null)
      <p class="md:mt-4 p-4 bg-green-100 text-green-700 rounded-md">${confirmationMessage}</p>
      <div class="flex justify-end gap-2">
        <button type="button"
                onclick="billetterie.closeModal();"
                class="bg-gray-300 text-gray-700 px-3 py-1 rounded hover:bg-gray-400 focus:outline-none focus:ring">Close</button>
      </div>
    @else
      <fieldset>
        <input type="hidden" name="type" value="${type.name()}" />
        @if (type == org.montrealjug.billetterie.ui.BookerController.BookerFormType.CREATE)
        <div>
          <label for="first-name" class="block text-gray-700">First Name</label>
          <input type="text"
                 id="first-name"
                 name="firstName"
                 class="border border-gray-300 rounded px-3 py-2 w-full"
                 value="${firstName}"
                 required>
        </div>
        <div>
          <label for="last-name" class="block text-gray-700">Last Name</label>
          <input type="text"
                 id="last-name"
                 name="lastName"
                 class="border border-gray-300 rounded px-3 py-2 w-full"
                 value="${lastName}"
                 required>
        </div>
        @endif
        <div>
          <label for="email" class="block text-gray-700">Email</label>
          @if (type == org.montrealjug.billetterie.ui.BookerController.BookerFormType.CHECK)
          <input type="email"
                 id="email"
                 name="email"
                 class="border border-gray-300 rounded px-3 py-2 w-full" required>
          @else
          <input type="email"
                 id="email"
                 name="email"
                 value="${email}"
                 class="border border-gray-300 rounded px-3 py-2 w-full" required
          >
          @endif
        </div>
        @if (type == org.montrealjug.billetterie.ui.BookerController.BookerFormType.CREATE)
        <div>
          <p id="none-matching-email" class="hidden text-red-500 text-sm">None matching email</p>
          <label for="confirm-email" class="block text-gray-700">Confirm Email</label>
          <input type="email"
                 id="confirm-email"
                 name="confirmEmail"
                 class="border border-gray-300 rounded px-3 py-2 w-full"
                 required
                 hx-on:htmx:validation:validate="if (this.value !== document.getElementById('email').value) {
                    document.getElementById('none-matching-email').classList.remove('hidden');
                 } else {
                    document.getElementById('none-matching-email').classList.add('hidden');
                 }">
        </div>
        @endif
      </fieldset>
      <div class="flex justify-end gap-2">
        <button type="button" onclick="billetterie.closeModal();" class="bg-gray-300 text-gray-700 px-3 py-1 rounded hover:bg-gray-400 focus:outline-none focus:ring">Cancel</button>
        <button type="submit" class="bg-d4k-orange text-white px-3 py-1 rounded hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300">Submit</button>
      </div>
    </form>
    @endif
  </div>
</div>
