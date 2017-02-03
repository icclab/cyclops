def generate_usages(number_of_records, size_of_metadata, unique_metadata, unique_users):
    average_number_of_records = number_of_records/unique_users
    time = 0
    list_of_usages = []
    rest = number_of_records - average_number_of_records * unique_users

    for user in range(unique_users):
        additional = 0
        if rest > 0:
            additional = 1
        if number_of_records >= average_number_of_records:
            number_of_records -= average_number_of_records
        else:
            average_number_of_records = number_of_records
        for record in range(average_number_of_records + additional):
            usage = {}
            metadata = {}

            if record >= (average_number_of_records+ additional)*unique_metadata/100:
                should_be_unique = False
            else:
                should_be_unique = True

            if should_be_unique:
                for j in range(size_of_metadata):
                    metadata[str(time) + str(j)] = str(time) + str(j)
            else:
                for j in range(size_of_metadata):
                    metadata[str(j)] = str(j)

            usage["unit"] = "s"
            usage["usage"] = 10
            usage["_class"] = "OpenStackCeilometerCpu"
            usage["account"] = str(user)
            usage["time"] = time
            if metadata:
                usage["metadata"] = metadata
            list_of_usages.append(usage)
            time += 1

        rest -= 1

    return list_of_usages


